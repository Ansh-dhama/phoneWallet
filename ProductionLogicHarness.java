import com.example.phoneWallet.Exceptions.*;
import com.example.phoneWallet.Repository.*;
import com.example.phoneWallet.Util.IdempotencyKeyUtil;
import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.*;
import com.example.phoneWallet.enums.*;
import com.example.phoneWallet.services.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.*;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class ProductionLogicHarness {
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static void money(BigDecimal actual, String expected, String message) {
        check(actual.compareTo(new BigDecimal(expected)) == 0, message + " expected=" + expected + " actual=" + actual);
    }
    static void expect(Class<? extends Throwable> type, Runnable work, String message) {
        try { work.run(); }
        catch (Throwable t) {
            Throwable x = t instanceof RuntimeException && t.getCause() != null ? t.getCause() : t;
            if (type.isInstance(x) || type.isInstance(t)) return;
            throw new AssertionError(message + " wrong exception=" + t, t);
        }
        throw new AssertionError(message + " did not throw");
    }

    static class TxStore implements InvocationHandler {
        final AtomicLong ids = new AtomicLong(1);
        final Map<Long, Transaction> byId = new HashMap<>();
        final Map<String, Transaction> byKey = new HashMap<>();
        final Map<String, Transaction> byRef = new HashMap<>();
        TransactionRepository proxy() { return (TransactionRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{TransactionRepository.class}, this); }
        public Object invoke(Object proxy, Method m, Object[] a) {
            String n=m.getName();
            if (n.equals("save") || n.equals("saveAndFlush")) {
                Transaction t=(Transaction)a[0];
                if (t.getId()==null) t.setId(ids.getAndIncrement());
                byId.put(t.getId(),t); byKey.put(t.getIdempotencyKey(),t); byRef.put(t.getTransactionReference(),t); return t;
            }
            if (n.equals("findByIdempotencyKey")) return Optional.ofNullable(byKey.get((String)a[0]));
            if (n.equals("findByTransactionReference") || n.equals("findByTransactionReferenceForUpdate")) return Optional.ofNullable(byRef.get((String)a[0]));
            if (n.equals("findById")) return Optional.ofNullable(byId.get((Long)a[0]));
            if (n.equals("count")) return (long)byId.size();
            if (n.equals("toString")) return "TxStore";
            throw new UnsupportedOperationException("TxRepo."+n);
        }
    }

    static class LedgerStore implements InvocationHandler {
        final List<LedgerEntry> entries = new ArrayList<>();
        LedgerRepository proxy() { return (LedgerRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{LedgerRepository.class}, this); }
        public Object invoke(Object proxy, Method m, Object[] a) {
            String n=m.getName();
            if (n.equals("save")) { entries.add((LedgerEntry)a[0]); return a[0]; }
            if (n.equals("flush")) return null;
            if (n.equals("findByTransactionId")) return entries.stream().filter(e->e.getTransactionId().equals(a[0])).toList();
            if (n.equals("findByWalletId")) return entries.stream().filter(e->Objects.equals(e.getWalletId(), a[0])).toList();
            if (n.equals("sumAmountByTransactionIdAndTypes")) {
                String ref=(String)a[0]; @SuppressWarnings("unchecked") List<String> types=(List<String>)a[1];
                return entries.stream().filter(e->e.getTransactionId().equals(ref) && types.contains(e.getEntryType().name()))
                        .map(LedgerEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            if (n.equals("toString")) return "LedgerStore";
            throw new UnsupportedOperationException("LedgerRepo."+n);
        }
    }

    static class TopUpStore implements InvocationHandler {
        final AtomicLong ids = new AtomicLong(100);
        final Map<Long, TopUpIntent> byId = new HashMap<>();
        final Map<String, TopUpIntent> byKey = new HashMap<>();
        final Map<String, TopUpIntent> byOrder = new HashMap<>();
        TopUpIntentRepository proxy() { return (TopUpIntentRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{TopUpIntentRepository.class}, this); }
        public Object invoke(Object proxy, Method m, Object[] a) {
            String n=m.getName();
            if (n.equals("save")) {
                TopUpIntent t=(TopUpIntent)a[0]; if (t.getId()==null) t.setId(ids.getAndIncrement());
                byId.put(t.getId(),t); byKey.put(t.getIdempotencyKey(),t); byOrder.put(t.getProviderOrderId(),t); return t;
            }
            if (n.equals("findByIdempotencyKey")) return Optional.ofNullable(byKey.get((String)a[0]));
            if (n.equals("findByIdForUpdate")) return Optional.ofNullable(byId.get((Long)a[0]));
            if (n.equals("findByProviderOrderIdForUpdate") || n.equals("findByProviderOrderId")) return Optional.ofNullable(byOrder.get((String)a[0]));
            if (n.equals("toString")) return "TopUpStore";
            throw new UnsupportedOperationException("TopUpRepo."+n);
        }
    }

    static class FakeWalletService extends WalletService {
        final Map<Long, Wallet> wallets = new HashMap<>();
        FakeWalletService(){ super(null,null,null,null); }
        void add(Wallet w){ wallets.put(w.getId(), w); }
        @Override public Wallet findById(Long id){ Wallet w=wallets.get(id); if(w==null) throw new WalletNotFoundException(id); return w; }
        @Override public Wallet findByIdForUpdate(Long id){ return findById(id); }
        @Override public synchronized Map<Long,Wallet> lockWalletsInOrder(Long a, Long b){
            Map<Long,Wallet> r=new HashMap<>(); r.put(a,findById(a)); r.put(b,findById(b)); return r;
        }
        @Override public synchronized Wallet credit(Long id, BigDecimal amount){ return creditLocked(findById(id), amount); }
        @Override public synchronized Wallet debit(Long id, BigDecimal amount){ return debitLocked(findById(id), amount); }
        @Override public synchronized Wallet creditLocked(Wallet w, BigDecimal amount){
            if(w.getStatus()!=WalletStatus.ACTIVE) throw new WalletFrozenException("Wallet is not active");
            w.setBalance(w.getBalance().add(amount)); return w;
        }
        @Override public synchronized Wallet debitLocked(Wallet w, BigDecimal amount){
            if(w.getStatus()!=WalletStatus.ACTIVE) throw new WalletFrozenException("Wallet is not active");
            if(amount.compareTo(w.getBalance())>0) throw new InsufficentAmountException("Insufficient funds");
            w.setBalance(w.getBalance().subtract(amount)); return w;
        }
    }

    static class FakeAccess extends AccessControlService {
        User actor;
        final Map<Long,Wallet> wallets; final Map<Long,User> users;
        FakeAccess(Map<Long,Wallet> wallets, Map<Long,User> users){ super(null,null,null); this.wallets=wallets; this.users=users; }
        void actor(User u){ actor=u; }
        @Override public User currentUser(){ return actor; }
        @Override public Wallet requireOwnedWallet(Long id){ Wallet w=wallets.get(id); if(w==null) throw new WalletNotFoundException(id); if(!Objects.equals(w.getUserId(),actor.getId())) throw new AccessDeniedException("not owner"); return w; }
        @Override public Wallet requireWalletAccess(Long id){ Wallet w=wallets.get(id); if(w==null) throw new WalletNotFoundException(id); if(actor.getRole()!=Role.ADMIN && !Objects.equals(w.getUserId(),actor.getId())) throw new AccessDeniedException("no access"); return w; }
        @Override public Wallet requireMerchantWallet(Long id){ Wallet w=wallets.get(id); if(w==null) throw new WalletNotFoundException(id); User owner=users.get(w.getUserId()); if(owner==null||owner.getRole()!=Role.MERCHANT) throw new AccessDeniedException("not merchant"); return w; }
        @Override public void requireTransactionAccess(Transaction t){ if(actor.getRole()==Role.ADMIN) return; if(owns(t.getFromWalletId())||owns(t.getToWalletId())) return; throw new AccessDeniedException("no tx access"); }
        @Override public void requireRefundAccess(Transaction t){
            if(actor.getRole()==Role.ADMIN) return;
            if(actor.getRole()!=Role.MERCHANT || t.getType()!=TransactionType.MERCHANT_PAYMENT || !owns(t.getToWalletId())) throw new AccessDeniedException("no refund access");
        }
        boolean owns(Long wid){ return wid!=null && wallets.containsKey(wid) && Objects.equals(wallets.get(wid).getUserId(),actor.getId()); }
    }

    static class FakeRisk extends RiskService { FakeRisk(){ super(null,null,null,new BigDecimal("50000")); } @Override public void validateRisk(Long id, BigDecimal a){} }
    static class FakeAuditLog extends AuditLogService { FakeAuditLog(){ super(null); }
        @Override public void logSuccess(AuditAction a,Long u,String n,Role r,Long w,String t,String d){}
        @Override public void logFailure(AuditAction a,Long u,String n,Role r,Long w,String t,String d,String f){}
    }
    static class FakeOutbox extends OutboxEventService { int count; FakeOutbox(){ super(null,null); } @Override public void saveEvent(String e,String t,Object p){ count++; } }
    static class FakeLock extends DistributedLockService { FakeLock(){ super(null); } @Override public synchronized <T>T withLock(String k, Duration w, Duration l, Supplier<T> work){ return work.get(); } }
    static class NoopTM implements PlatformTransactionManager {
        public org.springframework.transaction.TransactionStatus getTransaction(TransactionDefinition d){ return new SimpleTransactionStatus(); }
        public void commit(org.springframework.transaction.TransactionStatus s){}
        public void rollback(org.springframework.transaction.TransactionStatus s){}
    }

    static User user(long id,String name,Role role){ User u=new User();u.setId(id);u.setUsername(name);u.setMobileNumber("900000000"+id);u.setRole(role);u.setPassword("x");return u; }
    static Wallet wallet(long id,long uid,String currency,String bal){ Wallet w=new Wallet();w.setId(id);w.setUserId(uid);w.setWalletNumber("W"+id);w.setCurrency(currency);w.setBalance(new BigDecimal(bal));w.setStatus(WalletStatus.ACTIVE);return w; }

    public static void main(String[] args) {
        // actor-scoped idempotency
        String k1=IdempotencyKeyUtil.scoped(1L,"same"); String k2=IdempotencyKeyUtil.scoped(2L,"same");
        check(!k1.equals(k2) && k1.length()==64, "scoped idempotency hash");

        User u1=user(1,"u1",Role.USER), u2=user(2,"u2",Role.USER), merchant=user(3,"m",Role.MERCHANT), admin=user(99,"admin",Role.ADMIN);
        Map<Long,User> users=new HashMap<>(); for(User u:List.of(u1,u2,merchant,admin)) users.put(u.getId(),u);
        FakeWalletService ws=new FakeWalletService();
        Wallet w1=wallet(11,1,"INR","1000"), w2=wallet(22,2,"INR","100"), wm=wallet(33,3,"INR","0"), wUsd=wallet(44,2,"USD","100");
        ws.add(w1);ws.add(w2);ws.add(wm);ws.add(wUsd);
        FakeAccess access=new FakeAccess(ws.wallets,users); access.actor(u1);
        TxStore txs=new TxStore(); TransactionRepository tr=txs.proxy();
        LedgerStore ls=new LedgerStore(); LedgerRepository lr=ls.proxy(); LedgerService ledger=new LedgerService(lr); AuditLedgerService audit=new AuditLedgerService(lr);
        FakeOutbox outbox=new FakeOutbox();
        TransactionService svc=new TransactionService(tr,ws,ledger,audit,new FakeRisk(),new FakeAuditLog(),outbox,access,new FakeLock(),new NoopTM());

        TransferRequest t=new TransferRequest(); t.setFromWalletId(11L);t.setToWalletId(22L);t.setAmount(new BigDecimal("200"));t.setCurrency("INR");t.setIdempotencyKey("transfer-key");
        Transaction transfer=svc.processTransaction(t);
        check(transfer.getStatus()==com.example.phoneWallet.enums.TransactionStatus.SUCCESS,"transfer success"); money(w1.getBalance(),"800","transfer sender"); money(w2.getBalance(),"300","transfer receiver"); check(audit.verifyTransaction(transfer.getTransactionReference()),"transfer ledger balanced");
        Transaction retry=svc.processTransaction(t); check(retry.getId().equals(transfer.getId()),"idempotent retry same transaction"); money(w1.getBalance(),"800","retry no double debit");
        TransferRequest changed=new TransferRequest();changed.setFromWalletId(11L);changed.setToWalletId(22L);changed.setAmount(new BigDecimal("201"));changed.setCurrency("INR");changed.setIdempotencyKey("transfer-key");
        expect(InvalidTransactionException.class,()->svc.processTransaction(changed),"idempotency semantic mismatch rejected");

        TransferRequest bola=new TransferRequest();bola.setFromWalletId(22L);bola.setToWalletId(11L);bola.setAmount(new BigDecimal("10"));bola.setCurrency("INR");bola.setIdempotencyKey("bola");
        expect(AccessDeniedException.class,()->svc.processTransaction(bola),"BOLA transfer rejected");

        TransferRequest sameWallet=new TransferRequest();sameWallet.setFromWalletId(11L);sameWallet.setToWalletId(11L);sameWallet.setAmount(new BigDecimal("1"));sameWallet.setCurrency("INR");sameWallet.setIdempotencyKey("same-wallet");
        expect(IllegalArgumentException.class,()->svc.processTransaction(sameWallet),"same-wallet transfer rejected");

        TransferRequest currencyMismatch=new TransferRequest();currencyMismatch.setFromWalletId(11L);currencyMismatch.setToWalletId(44L);currencyMismatch.setAmount(new BigDecimal("1"));currencyMismatch.setCurrency("INR");currencyMismatch.setIdempotencyKey("currency-mismatch");
        expect(InvalidTransactionException.class,()->svc.processTransaction(currencyMismatch),"currency mismatch rejected");

        TransferRequest insufficient=new TransferRequest();insufficient.setFromWalletId(11L);insufficient.setToWalletId(22L);insufficient.setAmount(new BigDecimal("5000"));insufficient.setCurrency("INR");insufficient.setIdempotencyKey("insufficient");
        expect(InsufficentAmountException.class,()->svc.processTransaction(insufficient),"insufficient balance rejected");

        w1.setStatus(WalletStatus.FROZEN);
        TransferRequest frozen=new TransferRequest();frozen.setFromWalletId(11L);frozen.setToWalletId(22L);frozen.setAmount(new BigDecimal("1"));frozen.setCurrency("INR");frozen.setIdempotencyKey("frozen");
        expect(WalletFrozenException.class,()->svc.processTransaction(frozen),"frozen wallet movement rejected");
        w1.setStatus(WalletStatus.ACTIVE);

        TransferRequest missingWallet=new TransferRequest();missingWallet.setFromWalletId(11L);missingWallet.setToWalletId(999L);missingWallet.setAmount(new BigDecimal("1"));missingWallet.setCurrency("INR");missingWallet.setIdempotencyKey("missing-wallet");
        expect(WalletNotFoundException.class,()->svc.processTransaction(missingWallet),"missing destination wallet rejected");

        PayRequest badPay=new PayRequest();badPay.setFromWalletId(11L);badPay.setToWalletId(22L);badPay.setAmount(new BigDecimal("10"));badPay.setCurrency("INR");badPay.setIdempotencyKey("badpay");
        expect(AccessDeniedException.class,()->svc.payTransaction(badPay),"non-merchant payee rejected");

        PayRequest pay=new PayRequest();pay.setFromWalletId(11L);pay.setToWalletId(33L);pay.setAmount(new BigDecimal("100"));pay.setCurrency("INR");pay.setIdempotencyKey("pay1");pay.setMerchantReference("INV-1");
        Transaction payment=svc.payTransaction(pay); money(w1.getBalance(),"700","payment payer"); money(wm.getBalance(),"100","payment merchant"); check(audit.verifyTransaction(payment.getTransactionReference()),"payment ledger balanced");

        RefundRequest unauthorizedRefund=new RefundRequest();unauthorizedRefund.setOriginalTransactionReference(payment.getTransactionReference());unauthorizedRefund.setRefundAmount(new BigDecimal("1"));unauthorizedRefund.setCurrency("INR");unauthorizedRefund.setIdempotencyKey("user-refund");
        expect(AccessDeniedException.class,()->svc.refund(unauthorizedRefund),"normal user cannot issue merchant refund");

        access.actor(merchant);
        RefundRequest r1=new RefundRequest();r1.setOriginalTransactionReference(payment.getTransactionReference());r1.setRefundAmount(new BigDecimal("30"));r1.setCurrency("INR");r1.setIdempotencyKey("refund1");
        Transaction ref1=svc.refund(r1); check(payment.getStatus()==com.example.phoneWallet.enums.TransactionStatus.PARTIALLY_REFUNDED,"partial refund status");money(payment.getRefundedAmount(),"30","partial refunded amount");money(wm.getBalance(),"70","merchant after partial");money(w1.getBalance(),"730","payer after partial");
        expect(AmountMismatch.class,()->{ RefundRequest over=new RefundRequest();over.setOriginalTransactionReference(payment.getTransactionReference());over.setRefundAmount(new BigDecimal("80"));over.setCurrency("INR");over.setIdempotencyKey("refund-over");svc.refund(over);},"over refund rejected");
        RefundRequest r2=new RefundRequest();r2.setOriginalTransactionReference(payment.getTransactionReference());r2.setRefundAmount(new BigDecimal("70"));r2.setCurrency("INR");r2.setIdempotencyKey("refund2");
        svc.refund(r2); check(payment.getStatus()==com.example.phoneWallet.enums.TransactionStatus.REFUNDED,"full cumulative refund status");money(payment.getRefundedAmount(),"100","full refunded amount");money(wm.getBalance(),"0","merchant after full refund");money(w1.getBalance(),"800","payer after full refund");
        expect(TransactionNotComplete.class,()->{ RefundRequest afterFull=new RefundRequest();afterFull.setOriginalTransactionReference(payment.getTransactionReference());afterFull.setRefundAmount(new BigDecimal("1"));afterFull.setCurrency("INR");afterFull.setIdempotencyKey("refund3");svc.refund(afterFull);},"refund after fully refunded rejected");

        access.actor(admin);
        ReversalRequest rev=new ReversalRequest();rev.setOriginalTransactionReference(transfer.getTransactionReference());rev.setReason("admin correction");rev.setIdempotencyKey("rev1");
        Transaction reversal=svc.reverseTransaction(rev); check(reversal.getStatus()==com.example.phoneWallet.enums.TransactionStatus.SUCCESS,"reversal success"); check(transfer.getStatus()==com.example.phoneWallet.enums.TransactionStatus.REVERSED,"original reversed");money(w1.getBalance(),"1000","reversal source restored");money(w2.getBalance(),"100","reversal dest restored");check(audit.verifyTransaction(reversal.getTransactionReference()),"reversal ledger balanced");
        svc.reverseTransaction(rev); money(w1.getBalance(),"1000","idempotent reversal retry");
        ReversalRequest refundedReversal=new ReversalRequest();refundedReversal.setOriginalTransactionReference(payment.getTransactionReference());refundedReversal.setReason("should fail");refundedReversal.setIdempotencyKey("rev-refunded");
        expect(InvalidTransactionException.class,()->svc.reverseTransaction(refundedReversal),"fully refunded payment cannot be reversed");

        // Top-up: intent does not credit; verified demo settlement credits exactly once.
        TopUpStore tus=new TopUpStore(); TopUpIntentRepository tur=tus.proxy();
        TopUpSignatureService sig=new TopUpSignatureService("12345678901234567890123456789012");
        TopUpService topups=new TopUpService(tur,tr,ws,access,ledger,audit,outbox,new FakeAuditLog(),sig,true,new BigDecimal("50000"));
        access.actor(u1);
        expect(InvalidTopUpException.class,()->topups.initiate(11L,new TopUpIntentRequest(new BigDecimal("50001"),"INR","too-large")),"topup max amount enforced");
        expect(InvalidTopUpException.class,()->topups.initiate(11L,new TopUpIntentRequest(new BigDecimal("10"),"USD","wrong-currency")),"topup wallet currency enforced");
        TopUpIntentRequest tiReq=new TopUpIntentRequest(new BigDecimal("500"),"INR","topup-client-1");
        TopUpIntentResponse ti=topups.initiate(11L,tiReq); money(w1.getBalance(),"1000","intent does not mint money");check(ti.status()==TopUpStatus.PENDING,"intent pending");
        TopUpIntentResponse tiRetry=topups.initiate(11L,tiReq);check(tiRetry.id().equals(ti.id()),"topup idempotent retry");
        expect(InvalidTopUpException.class,()->topups.initiate(11L,new TopUpIntentRequest(new BigDecimal("501"),"INR","topup-client-1")),"topup semantic mismatch rejected");
        TopUpIntentResponse done=topups.completeDemo(ti.id());check(done.status()==TopUpStatus.COMPLETED,"topup completed");money(w1.getBalance(),"1500","verified topup credits");
        topups.completeDemo(ti.id());money(w1.getBalance(),"1500","duplicate topup completion no double credit");

        TopUpWebhookRequest wh=new TopUpWebhookRequest("ORDER-X","PAY-X","SUCCESS",new BigDecimal("1.00"),"INR");String signature=sig.sign(wh);sig.verify(wh,signature);
        expect(InvalidTopUpException.class,()->sig.verify(new TopUpWebhookRequest("ORDER-X","PAY-X","SUCCESS",new BigDecimal("2.00"),"INR"),signature),"tampered webhook rejected");

        System.out.println("ALL CORE WALLET + NEGATIVE-PATH HARNESS TESTS PASSED");
        System.out.println("transactions="+txs.byId.size()+", ledgerEntries="+ls.entries.size()+", outboxEvents="+outbox.count);
    }
}
