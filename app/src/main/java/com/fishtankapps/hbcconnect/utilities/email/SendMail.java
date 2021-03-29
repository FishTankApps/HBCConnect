package com.fishtankapps.hbcconnect.utilities.email;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.fishtankapps.hbcconnect.utilities.Constants;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail extends AsyncTask<Object, Object, Object> {

   @SuppressLint("StaticFieldLeak")
   private final Activity activity;
   private final String subject;
   private final String message;
   private ProgressDialog progressDialog;

   public SendMail(Activity activity, String subject, String message){
      super();
      this.activity = activity;
      this.subject = subject;
      this.message = message;
   }


   @Override
   protected void onPreExecute() {
      super.onPreExecute();
      progressDialog = ProgressDialog.show(activity,"Sending Virtual Count Me In Card","Please wait...",false,false);
   }

   @Override
   protected void onPostExecute(Object aVoid) {
      super.onPostExecute(aVoid);
      progressDialog.dismiss();
      Toast.makeText(activity,"Count Me In Submitted!",Toast.LENGTH_LONG).show();
      activity.finish();
   }

   @Override
   protected Object doInBackground(Object[] objects) {
      Properties props = new Properties();
      props.put("mail.smtp.host", "smtp.gmail.com");
      props.put("mail.smtp.socketFactory.port", "465");
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.port", "465");
      Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(Constants.FISH_TANK_APPS_EMAIL, Constants.FISH_TANK_APPS_EMAIL_PASSWORD);
         }
      });

      try {
         MimeMessage mm = new MimeMessage(session);
         mm.setFrom(new InternetAddress(Constants.FISH_TANK_APPS_EMAIL));
         mm.addRecipient(Message.RecipientType.TO, new InternetAddress(Constants.HBC_ADMIN_EMAIL));
         mm.setSubject(subject);
         mm.setContent(message, "text/html; charset=utf-8");

         Transport.send(mm);
      } catch (MessagingException e) {
         e.printStackTrace();
      }

      return null;
   }
}