package com.moutamid.telegramdummy.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.adapter.ChatAdapter;
import com.moutamid.telegramdummy.databinding.FragmentChatBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class ChatFragment extends Fragment {
    FragmentChatBinding binding;
    ArrayList<ChatModel> list;
    public ChatAdapter adapter;
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
        getChat();
    }

    private static final String TAG = "ChatFragment";
    private void getChat() {
        list = Stash.getArrayList(Constants.USER, ChatModel.class);

//        if (list.isEmpty()) {
//            list.add(new ChatModel(
//                    "+123456789",
//                    1,
//                    "Moutamid",
//                    "",
//                    "Moutamid joined Telegram",
//                    1726469485971L,
//                    "Online",
//                    -6730518
//            ));
//            Stash.put(Constants.USER, list);
//        }

        list.sort(Comparator.comparing(ChatModel::getTimestamp));
        Collections.reverse(list);
        adapter = new ChatAdapter(requireContext(), requireActivity(), list);
        binding.chatRC.setAdapter(adapter);
    }
}