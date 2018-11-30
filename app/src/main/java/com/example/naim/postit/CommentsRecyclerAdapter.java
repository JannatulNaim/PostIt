package com.example.naim.postit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;
    public FirebaseFirestore firebaseFirestore;
    public long millisecond;

    public CommentsRecyclerAdapter(List<Comments> commentsList){

        this.commentsList = commentsList;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        firebaseFirestore = FirebaseFirestore.getInstance();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        String commentMessage = commentsList.get(position).getMessage();
        String UserId = commentsList.get(position).getUser_id();
        holder.setComment_message(commentMessage);
        holder.setUserName(UserId);

        try {
            if (commentsList.get(position).gettimestamp() != null) {
                millisecond = commentsList.get(position).gettimestamp().getTime();
                CommentTime commentTime = new CommentTime();
                String lasttime = commentTime.CommentTime(millisecond,context);
                
                holder.setTime(lasttime);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private TextView comment_UserName;
        private ImageView comment_image;
        private TextView comment_time;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setUserName(String UserId) {
            firebaseFirestore.collection("Users").document(UserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(task.getResult().exists()){

                            String name = task.getResult().getString("name");
                            String image = task.getResult().getString("image");
                            comment_UserName = mView.findViewById(R.id.comment_username);
                            comment_UserName.setText(name);
                            comment_image = mView.findViewById(R.id.comment_image);
                            Glide.with(context).load(image).into(comment_image);
                        }

                    } else {

                        String error = task.getException().getMessage();
                        Toast.makeText(context, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                    }

                }
            });

        }

        public void setTime(String dateString) {
            comment_time = mView.findViewById(R.id.comment_time);
            comment_time.setText(dateString);
        }
    }

}
