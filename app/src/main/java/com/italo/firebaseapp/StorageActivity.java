package com.italo.firebaseapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.italo.firebaseapp.model.Upload;
import com.italo.firebaseapp.util.LoadingDialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

public class StorageActivity extends AppCompatActivity {
    //Firebase Storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Button btnUpload, btnGaleria;
    private ImageView imageView;
    private Uri imageUri=null;
    private EditText editNome;
    // referencia p/ um nó RealtimeDB
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("upload");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        imageView = findViewById(R.id.storage_image_cel);
        btnUpload = findViewById(R.id.storage_btn_upload);
        btnGaleria = findViewById(R.id.storage_btn_galeria);
        editNome = findViewById(R.id.storage_edit_nome);
        btnUpload.setOnClickListener(v -> {
            if(editNome.getText().toString().isEmpty()){
                Toast.makeText(this, "Digite um nome para a imagem", Toast.LENGTH_SHORT).show();
                return;
            }
            if(imageUri!=null){
                uploadImagemUri();
            }else{
                uploadImagemByte();
            }
        });
        btnGaleria.setOnClickListener(v -> {
            Intent intent = new Intent();
            //intent implicita -> pegar um arquivo do celular
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            //inicia uma Activity, e espera o retorno da foto
            startActivityForResult(intent, 111);
        });
    }

    private void uploadImagemUri() {
        LoadingDialog dialog = new LoadingDialog(this, R.layout.custom_dialog);
        dialog.startLoadingDialog();
        String tipo = getFileExtension(imageUri);
        Date d = new Date();
        String nome = editNome.getText().toString();
        //criando referencia da imagem no Storage
        StorageReference imagemRef = storage.getReference().child("imagens/"+nome+"-"+d.getTime()+"."+tipo);
        imagemRef.putFile(imageUri)
        .addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Upload feito com sucesso", Toast.LENGTH_SHORT).show();

            // Inserir dados da imagem na Realtime database
            //pegar a URL da imagem
            taskSnapshot.getStorage().getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        // Inserir no Database
                        // criando refrencia(database) do upload
                        DatabaseReference refUpload = database.push();
                        String id = refUpload.getKey();
                        Upload upload = new Upload(id,nome,uri.toString());

                        //salvando no database
                        refUpload.setValue(upload).addOnSuccessListener(aVoid -> {
                            dialog.dismissDialog();
                            Toast.makeText(this, "Uplosd feito com sucesso", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
        })
        .addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }
    //retornar o tipo(.png .. jpg) da image,m
    private String getFileExtension(Uri imageUri) {
        ContentResolver cr = getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(imageUri));
    }
    //resultado da startActivity(Result)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("RESULT", "requestCode : "+requestCode +", resultCode: "+requestCode);
        if (requestCode==111 && resultCode== Activity.RESULT_OK){
            //caso o usuario selecionou uma imagem da galeria
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    public byte[] convertImageByte(ImageView imageView){
        //Converter ImageView -> byte[]
        Bitmap bitmap = ( (BitmapDrawable) imageView.getDrawable()  ).getBitmap();
        //objeto baos ->
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();

    }
    //fazer upload de uma convertida p/bytes
    public void  uploadImagemByte(){
        byte[] data = convertImageByte(imageView);

        //Criar uma referencia para imagem no Storage
        StorageReference imageRef = storage.getReference().child("imagens/01.jpeg");
        //Upload da imagem
        imageRef.putBytes(data)
        .addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Upload feito com sucesso", Toast.LENGTH_SHORT).show();
            Log.i("UPLOAD", "Sucesso");
        })
        .addOnFailureListener(e ->{
            e.printStackTrace();
        })
        ;
    }

}
