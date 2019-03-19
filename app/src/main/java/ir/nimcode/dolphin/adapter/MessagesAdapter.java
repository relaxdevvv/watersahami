//package ir.nimcode.dolphin.adapter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.support.v7.widget.CardView;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//import ir.nimcode.dolphin.R;
//import ir.nimcode.dolphin.model.Message;
//import ir.nimcode.dolphin.util.JalaliCalendar;
//
///**
// * Created by saeed on 11/26/17.
// */
//
//public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
//
//    private Context context;
//    private ArrayList<Message> list;
//
//    public MessagesAdapter(Context context, ArrayList<Message> list) {
//        this.context = context;
//        this.list = list;
//    }
//
//    @Override
//    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_message, parent, false));
//
//    }
//
//    @Override
//    public void onBindViewHolder(final MessageViewHolder holder, int position) {
//        final Message message = list.get(position);
//        JalaliCalendar createdAt = new JalaliCalendar(message.getCreatedTime());
//        holder.date.setText(createdAt.dateToString());
//        holder.time.setText(createdAt.timeToString());
//        holder.message.setText(message.get());
//        if (!message.getVisited()) {
//            holder.newMessage.setVisibility(View.VISIBLE);
//        }
//        holder.rootView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                message.setVisited(true);
//                holder.newMessage.setVisibility(View.INVISIBLE);
//                //TODO: update message visited
//
//                Intent intent = new Intent(context, RequestDetailsActivity.class);
//                intent.putExtra("request", message.getRequest());
//                intent.putExtra("suggestion", message.getSuggestion());
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    public class MessageViewHolder extends RecyclerView.ViewHolder {
//
//        private TextView date;
//        private TextView time;
//        private ImageView newMessage;
//        private TextView message;
//        private CardView rootView;
//
//        public MessageViewHolder(View itemView) {
//            super(itemView);
//            this.date = (TextView) itemView.findViewById(R.id.date);
//            this.time = (TextView) itemView.findViewById(R.id.time);
//            this.message = (TextView) itemView.findViewById(R.id.message);
//            this.newMessage = (ImageView) itemView.findViewById(R.id.new_message);
//            this.rootView = (CardView) itemView.findViewById(R.id.root_view);
//        }
//    }
//}
