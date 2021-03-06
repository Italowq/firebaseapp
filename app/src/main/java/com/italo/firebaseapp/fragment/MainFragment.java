package com.italo.firebaseapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.provider.ContactsContract;
import android.service.autofill.Dataset;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.italo.firebaseapp.R;
import com.italo.firebaseapp.adapter.UserAdapter;
import com.italo.firebaseapp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainFragment extends Fragment {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private RecyclerView recyclerContatos;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef  = FirebaseDatabase.getInstance()
            .getReference("users");
    private DatabaseReference requestRef  = FirebaseDatabase.getInstance()
            .getReference("requests");
    private ArrayList<User> listaContatos = new ArrayList<>();
    private User userLogged;
    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_main, container, false);
        userLogged = new User(
                auth.getCurrentUser().getUid(),
                auth.getCurrentUser().getEmail(),
                auth.getCurrentUser().getDisplayName()
                );

        recyclerContatos = layout.findViewById(R.id.frag_main_recycler_user);
        userAdapter = new UserAdapter(getContext(),listaContatos);
        userAdapter.setListener(new UserAdapter.ClickAdapterUser() {
            @Override
            public void adicionarContatos(int position) {
                User u = listaContatos.get(position);
                //request send
                requestRef.child(userLogged.getId()).child("send").child(u.getId()).setValue(u);

                requestRef.child(u.getId()).child("receive").child(userLogged.getId()).setValue(userLogged);


                //Tirar o ??suario solicitado
                listaContatos.get(position).setReceiveRequest(true);
                userAdapter.notifyDataSetChanged();
            }
        });
        recyclerContatos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerContatos.setAdapter(userAdapter);


        return layout;
    }
    @Override
    public void onStart(){
        super.onStart();
        getUsersDatabase();
    }
    public void getUsersDatabase() {
        Map<String,User> mapUsersReq = new HashMap<String, User>();
        //Ir?? armazenar usuarios que ja foram solicitados
        requestRef.child(userLogged.getId()).child("send")
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot u : snapshot.getChildren()){
                    User user = u.getValue(User.class);
                    //adicionar usuario no HashMap
                    mapUsersReq.put(user.getId(), user);
                }
                //Ler o no dos usuarios
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaContatos.clear();
                        for(DataSnapshot u : snapshot.getChildren()){
                            User user = u.getValue(User.class);
                            if(mapUsersReq.containsKey(user.getId())){
                                user.setReceiveRequest(true);
                            }
                            if (!userLogged.equals(user)) {
                                listaContatos.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaContatos.clear();
                for(DataSnapshot filho : snapshot.getChildren()){
                    User u = filho.getValue(User.class);
                    // comparar com usuario logado
                    if(!userLogged.equals(u)){
                        if(cont%2==0){
                            u.setReceiveRequest(true);
                        }else{
                            u.setReceiveRequest(false);
                        }
                        listaContatos.add(u);
                    }
                }
                requestRef.child(userLogged.getId()).child("send").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot no_filho : snapshot.getChildren()){
                            User usuarioSolicitado = no_filho.getValue(User.class);
                            for(int i =0; i< listaContatos.size(); i++){
                                if(listaContatos.get(i).equals(usuarioSolicitado)){
                                    listaContatos.get(i).setReceiveRequest(true);
                                }
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

}
