package com.moutamid.telegramdummy.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.adapter.ChatAdapter;
import com.moutamid.telegramdummy.databinding.FragmentChatBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.ArrayList;

public class ChatFragment extends Fragment {
    FragmentChatBinding binding;
    ArrayList<ChatModel> list;
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(getLayoutInflater(), container, false);

        list = new ArrayList<>();

        binding.chatRC.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatRC.setHasFixedSize(false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Constants.initDialog(requireContext(), "Please Wait");
        getChat();
    }

    private void getChat() {
        Constants.showDialog();
        Constants.databaseReference().child(Constants.CHATS).child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Constants.dismissDialog();
                        if (snapshot.exists()){
                            list.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                ChatModel model = dataSnapshot.getValue(ChatModel.class);
                                list.add(model);
                            }

                            if (list.size() > 0){
                                binding.chatRC.setVisibility(View.VISIBLE);
                            } else {
                                binding.chatRC.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), "No Chat Found", Toast.LENGTH_SHORT).show();
                            }
                            ChatAdapter adapter = new ChatAdapter(requireContext(), list);
                            binding.chatRC.setAdapter(adapter);
                        } else {
                            Toast.makeText(requireContext(), "No Chat Found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}