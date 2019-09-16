package com.example.kristijan.opgwebshopadmin.Common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.Email.GMailSender;
import com.example.kristijan.opgwebshopadmin.Model.OrderRequest;

public class SendEmail {

    public SendEmail() {
    }

    public void sendEmail(final Context context, final String link, final OrderRequest order) {


        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String body="Poštovani"+" "+order.getUser().getName()+" "+order.getUser().getSurname()+"\n\nZahvaljujemo što kupujete kod nas. U nastavku možete pronaći detalje vaše narudžbe.\n\nDetalji narudžbe: "+link;

                    GMailSender sender = new GMailSender("kristijan.cotic993@gmail.com",
                            "torcida19500");
                    sender.sendMail("OPG WebShop", body,
                            "kristijan.cotic993@gmail.com", order.getEmail());
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                    Toast.makeText(context, "errr", Toast.LENGTH_SHORT).show();
                }
            }

        }).start();
    }
}
