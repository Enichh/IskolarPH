package com.example.iskolarphh.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iskolarphh.R;
import com.example.iskolarphh.model.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;

    private final List<ChatMessage> messages;
    private final SimpleDateFormat timeFormat;
    private final Markwon markwon;

    public MessageAdapter(android.content.Context context) {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        this.markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .build();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_assistant, parent, false);
            return new AssistantViewHolder(view, markwon);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String time = timeFormat.format(new Date(message.getTimestamp()));

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message, time);
        } else if (holder instanceof AssistantViewHolder) {
            ((AssistantViewHolder) holder).bind(message, time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final TextView textTime;

        UserViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessage message, String time) {
            textMessage.setText(message.getContent());
            textTime.setText(time);
        }
    }

    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final TextView textTime;
        private final Markwon markwon;

        AssistantViewHolder(View itemView, Markwon markwon) {
            super(itemView);
            this.markwon = markwon;
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessage message, String time) {
            markwon.setMarkdown(textMessage, message.getContent());
            textTime.setText(time);
        }
    }
}
