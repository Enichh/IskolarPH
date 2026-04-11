package com.example.iskolarphh.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iskolarphh.R;
import com.example.iskolarphh.api.RetrofitClient;
import com.example.iskolarphh.model.ChatMessage;
import com.example.iskolarphh.model.LongcatRequest;
import com.example.iskolarphh.model.LongcatResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotDialog extends DialogFragment {
    private static final String MODEL = "LongCat-Flash-Chat";
    private static final int MAX_TOKENS = 1000;
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_CONVERSATION_MESSAGES = 20;
    private static final int MAX_INPUT_LENGTH = 500;
    private static final long MIN_MESSAGE_INTERVAL_MS = 1000;

    private static final String SYSTEM_PROMPT = "You are IskolarPH AI, the official AI assistant for the IskolarPH scholarship discovery app. " +
            "Your role is to help Filipino students find and apply for scholarships. " +
            "RULES: 1) Only discuss scholarships, education, career guidance, and study tips. " +
            "2) If asked off-topic questions, politely redirect to scholarship topics. " +
            "3) Never ask for or store personal identifiable information (PII) like full names, addresses, IDs, or financial details. " +
            "4) Do not verify external links; warn users to verify official sources. " +
            "5) Be professional, encouraging, and culturally sensitive to Filipino students. " +
            "6) If unsure about specific scholarship details, advise users to check official sources.";

    private RecyclerView recyclerMessages;
    private MessageAdapter messageAdapter;
    private TextInputEditText editMessage;
    private FloatingActionButton btnSend;
    private ProgressBar progressBar;
    private View btnClose;

    private final List<LongcatRequest.Message> conversationHistory = new ArrayList<>();
    private long lastMessageTime = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_chatbot, null);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        initializeConversation();

        builder.setView(view);
        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void initViews(View view) {
        recyclerMessages = view.findViewById(R.id.recyclerMessages);
        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        progressBar = view.findViewById(R.id.progressBar);
        btnClose = view.findViewById(R.id.btnClose);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(requireContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnClose.setOnClickListener(v -> {
            hideKeyboard();
            dismiss();
        });
    }

    private void hideKeyboard() {
        if (editMessage != null && isAdded()) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
            }
        }
    }

    private void initializeConversation() {
        conversationHistory.clear();
        conversationHistory.add(new LongcatRequest.Message("system", SYSTEM_PROMPT));
        addWelcomeMessage();
    }

    private void addWelcomeMessage() {
        String welcome = "Hi! I'm IskolarPH AI. I can help you with scholarship searches, application tips, essay writing, and study advice. " +
                "Note: I may occasionally make mistakes. Always verify important information with official sources.";
        messageAdapter.addMessage(new ChatMessage(ChatMessage.ROLE_ASSISTANT, welcome));
    }

    private boolean isInputValid(String input) {
        if (input.length() > MAX_INPUT_LENGTH) {
            Toast.makeText(requireContext(), "Message too long. Please keep it under " + MAX_INPUT_LENGTH + " characters.", Toast.LENGTH_SHORT).show();
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime < MIN_MESSAGE_INTERVAL_MS) {
            Toast.makeText(requireContext(), "Please wait a moment before sending another message.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean checkConversationLimit() {
        if (conversationHistory.size() >= MAX_CONVERSATION_MESSAGES) {
            String limitMessage = "This conversation has reached the message limit. Starting fresh...";
            messageAdapter.addMessage(new ChatMessage(ChatMessage.ROLE_ASSISTANT, limitMessage));

            conversationHistory.clear();
            conversationHistory.add(new LongcatRequest.Message("system", SYSTEM_PROMPT));

            String resetWelcome = "Conversation reset. I'm IskolarPH AI. How can I help you today?";
            messageAdapter.addMessage(new ChatMessage(ChatMessage.ROLE_ASSISTANT, resetWelcome));
            scrollToBottom();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String userInput = editMessage.getText() != null ? editMessage.getText().toString().trim() : "";
        if (userInput.isEmpty()) {
            return;
        }

        if (!isInputValid(userInput)) {
            return;
        }

        if (checkConversationLimit()) {
            editMessage.setText(userInput);
            return;
        }

        lastMessageTime = System.currentTimeMillis();
        editMessage.setText("");
        messageAdapter.addMessage(new ChatMessage(ChatMessage.ROLE_USER, userInput));
        conversationHistory.add(new LongcatRequest.Message(ChatMessage.ROLE_USER, userInput));

        showLoading(true);

        LongcatRequest request = new LongcatRequest(MODEL, conversationHistory, MAX_TOKENS, TEMPERATURE);

        RetrofitClient.getApiService().sendMessage(request).enqueue(new Callback<LongcatResponse>() {
            @Override
            public void onResponse(@NonNull Call<LongcatResponse> call, @NonNull Response<LongcatResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String assistantResponse = response.body().getAssistantMessage();
                    if (!assistantResponse.isEmpty()) {
                        conversationHistory.add(new LongcatRequest.Message(ChatMessage.ROLE_ASSISTANT, assistantResponse));
                        messageAdapter.addMessage(new ChatMessage(ChatMessage.ROLE_ASSISTANT, assistantResponse));
                        scrollToBottom();
                    }
                } else {
                    showError("Sorry, I couldn't process your request. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LongcatResponse> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private void showLoading(boolean show) {
        new Handler(Looper.getMainLooper()).post(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            btnSend.setEnabled(!show);
        });
    }

    private void showError(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            messageAdapter.addMessage(new ChatMessage(ChatMessage.ROLE_ASSISTANT, message));
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            recyclerMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
}
