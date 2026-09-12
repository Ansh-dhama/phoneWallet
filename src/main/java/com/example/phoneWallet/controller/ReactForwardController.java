package com.example.phoneWallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * For BrowserRouter: a direct refresh on a React route must still return index.html.
 */
@Controller
public class ReactForwardController {

    @GetMapping({
            "/login",
            "/signup",
            "/dashboard",
            "/wallet",
            "/transfer",
            "/pay",
            "/transactions",
            "/statements",
            "/refund",
            "/admin"
    })
    public String forwardToReact() {
        return "forward:/index.html";
    }
}
