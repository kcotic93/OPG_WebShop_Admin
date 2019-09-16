package com.example.kristijan.opgwebshopadmin.PDF;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.Common.SendEmail;
import com.example.kristijan.opgwebshopadmin.Model.Order;
import com.example.kristijan.opgwebshopadmin.Model.OrderRequest;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Pdf {

    ByteArrayOutputStream output = null;
    FirebaseStorage storage;
    StorageReference storageReference;

    String curentFile;
    Uri photoURI;
    File pictureFile = null;

    OrderRequest orderRequest;
    String pos;
    Locale locale = new Locale("hr","HR");
    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

    public Pdf() {
    }

    public Pdf(OrderRequest orderRequest,String pos) {
        this.orderRequest = orderRequest;
        this.pos=pos;
    }

    public void cretaepdf(final Context context) throws IOException, DocumentException {

        PdfReader reader = new PdfReader(context.getResources().openRawResource(R.raw.opgtemplate4));

        pictureFile=createImageFile(context);
        PdfStamper stamper = new PdfStamper(reader,
                new FileOutputStream(pictureFile));
        AcroFields form = stamper.getAcroFields();
        form.setField("number", pos);
        form.setField("From", "OPG Prodaja");
        form.setField("seller", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        form.setField("name", orderRequest.getUser().getName()+" "+orderRequest.getUser().getSurname());
        if(orderRequest.getUser().getState() != null)
        {
            form.setField("address", orderRequest.getUser().getStreetHouseNum());
            form.setField("city", orderRequest.getUser().getPostalCode()+" "+orderRequest.getUser().getCity());
        }
        if(Integer.parseInt(orderRequest.getShipping())>0)
        {
            int suma=Integer.parseInt(orderRequest.getTotal())-Integer.parseInt(orderRequest.getShipping());
            form.setField("Postarina", fmt.format(Integer.parseInt(orderRequest.getShipping())));
            form.setField("Suma", fmt.format(suma));
        }

        int i=1;
        List<Order> order=orderRequest.getProducts();
        for(Order product:order)
        {
            form.setField("product"+String.valueOf(i), product.getProductName());
            form.setField("Quantity"+String.valueOf(i), product.getQuantity());
            form.setField("Unit"+String.valueOf(i), fmt.format(Integer.parseInt(product.getPrice())));
            int price =(Integer.parseInt(product.getPrice()))*(Integer.parseInt(product.getQuantity()));
            form.setField("productsum"+String.valueOf(i), fmt.format(price));
            i=i+1;
        }
        form.setField("Ukupno", fmt.format(Integer.parseInt(orderRequest.getTotal())));
        stamper.close();
        reader.close();

        photoURI=Uri.fromFile(pictureFile);

        upload(context);
    }

    private void upload(final Context context) throws IOException {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("Files/");

        String image_name = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("pdfs/" + image_name+".pdf");

        imageFolder.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String link = uri.toString();
                        SendEmail email=new SendEmail();
                        email.sendEmail(context,link,orderRequest);
                        pictureFile.delete();


                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.wrong, Toast.LENGTH_SHORT).show();
            }
        });


    }

    public File createImageFile (Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String PdfFile = "Order" + timeStamp+".pdf";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File pdf = File.createTempFile(PdfFile, ".pdf", storageDir);
        curentFile=pdf.getAbsolutePath();
        pdf.delete();
        return pdf;
    }
}
