package com.android.classifiedapp;

import android.app.Application;

import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.UserAction;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String clientId = "AQlOefvrxsESdnd_UV3h8C70_e7XRACjk5ODy_NJZUBAyygNaXKeYipCcZNU_tYTQvgZkfZWR2rcLvNQ";
        String returnUrl = "com.android.classifiedapp://paypalpay";

        PayPalCheckout.setConfig(new CheckoutConfig(this,
                clientId,
                Environment.SANDBOX,
                CurrencyCode.USD,
                UserAction.PAY_NOW,
                returnUrl));
    }
}
