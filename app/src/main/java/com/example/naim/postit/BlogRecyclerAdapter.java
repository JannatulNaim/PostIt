package com.example.naim.postit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = blog_list.get(position).getImage_url();
        String thumbUri = blog_list.get(position).getImage_thumb();

        holder.setBlogImage(image_url, thumbUri);

        String user_id = blog_list.get(position).getUser_id();

        if(user_id.equals(currentUserId)){
            holder.deletePost.setEnabled(true);
            holder.deletePost.setVisibility(View.VISIBLE);
        }

        try {

            if (blog_list.get(position).getTimestamp() != null) {
                long millisecond = blog_list.get(position).getTimestamp().getTime();
                String dateString = DateFormat.format("EEE, MMM d, ''yy", new Date(millisecond)).toString();

                holder.setTime(dateString);


            }

        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        if (blog_list.get(position).getTimestamp() != null) {
            long millisecond2 = blog_list.get(position).getTimestamp().getTime();
            CommentTime commentTime = new CommentTime();
            String lasttime = commentTime.CommentTime(millisecond2, context);
            holder.setLastTime(lasttime);
        }
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    String admin = task.getResult().getString("admin");
                    holder.admin.setText(admin);

                    holder.setUserData(userName, userImage);

                } else {

                    //Firebase Exception

                }

            }
        });


        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(documentSnapshots!=null && !documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });

        //Get Comment Count

        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(queryDocumentSnapshots!=null && !queryDocumentSnapshots.isEmpty()){

                    int count = queryDocumentSnapshots.size();

                    holder.updateCommentCount(count);

                } else {

                    holder.updateCommentCount(0);

                }

            }
        });


        //Get Likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot!= null && documentSnapshot.exists()){

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                } else {

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                }

            }
        });

        //Likes Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();

                        }

                    }
                });
            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);

            }
        });

        holder.deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setMessage("Do you want to Delete the Post ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Remove Like
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").
                                        addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                                                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                                        String LikeId = doc.getDocument().getId();
                                                        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(LikeId).delete();

                                                    }

                                                }
                                            }
                                        });


                                //Remove Comment
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").
                                        addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                                                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                                        String commentId = doc.getDocument().getId();
                                                        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").document(commentId).delete();

                                                    }

                                                }
                                            }
                                        });


                                //Remove Post
                                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        blog_list.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Post Deleted !!!", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog  = builder.create();
                alertDialog.setTitle("Delete Post !!!");
                alertDialog.setIcon(R.drawable.baseline_warning_black_24);
                alertDialog.show();

            }
        });

    }


    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView lastTime;
        private TextView admin;

        private TextView blogUserName;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        private TextView blogCommentCount;

        private ImageView blogCommentBtn;
        private Button deletePost;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            deletePost = mView.findViewById(R.id.btn_post_delete);
            admin = mView.findViewById(R.id.admin);

        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setBlogImage(String downloadUri, String thumbUri){

            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);

        }

        public void setTime(String date) {

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name, String image){

            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }

        public void updateLikesCount(int count){

            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");

        }

        public void setLastTime(String lasttime) {
            lastTime = mView.findViewById(R.id.last_time);
            if(lasttime==null){
                lastTime.setText("Posted - just now");
            }
            else {
                lastTime.setText("Posted - "+lasttime);
            }
        }

        public void updateCommentCount(int i) {
            blogCommentCount = mView.findViewById(R.id.blog_comment_count);
            blogCommentCount.setText(i + " Comments");
        }
    }

}
