package com.fishtankapps.hbcconnect.mobile.utilities.email;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.fishtankapps.hbcconnect.R;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail extends AsyncTask<Object, Object, Object> {

   private final ActivityGetter activityGetter;
   private final String subject, message, typeOfMessage, address;
   private ProgressDialog progressDialog;

   public SendMail(ActivityGetter activityGetter, String subject, String message, String typeOfMessage, String address){
      super();
      this.activityGetter = activityGetter;
      this.subject = subject;
      this.message = message;
      this.typeOfMessage = typeOfMessage;
      this.address = address;
   }


   @Override
   protected void onPreExecute() {
      super.onPreExecute();
      progressDialog = ProgressDialog.show(activityGetter.getActivity(),"Sending Virtual Count Me In Card","Please wait...",false,false);
   }

   @Override
   protected void onPostExecute(Object aVoid) {
      super.onPostExecute(aVoid);
      progressDialog.dismiss();
      Toast.makeText(activityGetter.getActivity(), typeOfMessage + " Submitted!",Toast.LENGTH_LONG).show();
      activityGetter.getActivity().finish();
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
            return new PasswordAuthentication(activityGetter.getActivity().getString(R.string.fishtankapps_donotreply_email), activityGetter.getActivity().getString(R.string.fishtankapps_email_password));
         }
      });

      try {
         MimeMessage mm = new MimeMessage(session);
         mm.setFrom(new InternetAddress(activityGetter.getActivity().getString(R.string.fishtankapps_donotreply_email)));
         mm.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
         mm.setSubject(subject);
         mm.setContent(message, "text/html; charset=utf-8");

         Transport.send(mm);
      } catch (MessagingException e) {
         e.printStackTrace();
      }

      return null;
   }


   public interface ActivityGetter {
      Activity getActivity();
   }
}