package com.italo.firebaseapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.italo.firebaseapp.R;
import com.italo.firebaseapp.adapter.ImageAdapter;
import com.italo.firebaseapp.model.Upload;

import java.util.ArrayList;


public class UploadsFragment extends Fragment {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference database  = FirebaseDatabase.getInstance()
            .getReference("uploads");
    private ArrayList<Upload> listaUploads = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    public UploadsFragment() {
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_uploads, container, false);
        recyclerView = layout.findViewById(R.id.main_recycler);

        return layout;
    }

}
