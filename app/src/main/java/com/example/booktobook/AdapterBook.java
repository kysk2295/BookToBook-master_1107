package com.example.booktobook;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

public class AdapterBook extends RecyclerView.Adapter<AdapterBook.ViewHolder> {

    private static ArrayList<BookData> bookDataSet;
    private static String id;
    private static String TIME;
    private static String PLACE;
    Context mcontext;


    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView cover;
        public TextView title;
        public TextView author;
        public TextView publisher;
        public TextView haver;
        public TextView place;
        public TextView time;
        public Button borrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.item_book_imageView_cover);
            title = itemView.findViewById(R.id.item_book_textView_title);
            author = itemView.findViewById(R.id.item_book_textView_author);
            publisher = itemView.findViewById(R.id.item_book_textView_publisher);
            haver = itemView.findViewById(R.id.item_book_textView_haver);
            place = itemView.findViewById(R.id.item_book_textView_place);
            time = itemView.findViewById(R.id.item_book_textView_time);
            borrow = itemView.findViewById(R.id.item_book_button_borrow);



            borrow.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ObsoleteSdkInt")
                @Override
                public void onClick(View view) {

                    //양 계정에 알림을 추가
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = db.collection("Users")
                            .document(haver.getText().toString().substring(3));
                    documentReference.update("alert", FieldValue.arrayUnion(
                            new Alert(
                                    place.getText().toString().substring(3),
                                    time.getText().toString().substring(3),
                                    "빌려줌",
                                    title.getText().toString(),
                                    id.toString()
                            )
                    ));


                    documentReference = db.collection("Users")
                            .document(id.toString());
                    documentReference.update("alert",FieldValue.arrayUnion(
                            new Alert(
                                    place.getText().toString().substring(3),
                                    time.getText().toString().substring(3),
                                    "빌림",
                                    title.getText().toString(),
                                    haver.getText().toString().substring(3)
                            )
                    ));

//                    documentReference=db.collection("Users")
//                            .document(id.toString());
//                    final ArrayList<String> finalFlag = new ArrayList<>();
//                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if (task.isSuccessful()){
//                                DocumentSnapshot snapshot=task.getResult();
//                                if (snapshot.exists()){
//                                    if (snapshot.getData().get("alert")!=null){
//                                        List list = (List) snapshot.getData().get("alert");
//                                        for (int i=0;i<list.size();i++){
//                                            HashMap map=(HashMap) list.get(i);
//                                            finalFlag.add(map.get("status").toString());
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    });
//
//                    int cnt=0;
//                    for (int n=0;n<finalFlag.size();n++){
//                        if (finalFlag.get(n).equals("빌려줌")){
//                                cnt=1;
//
//                        }
//                    }

                    // 알림

                    PendingIntent pendingIntent= PendingIntent.getActivity(mcontext,0,new Intent(mcontext,MainActivity.class),PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationCompat.Builder builder= new NotificationCompat.Builder(mcontext,"default");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle("책 빌림");
                    builder.setContentText(haver.getText().toString()+"님의 "+title.getText().toString()+" 책 을 빌렸습니다.");
                    builder.setContentIntent(pendingIntent);
                    builder.setAutoCancel(true);

                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                        NotificationManager notificationManager=(NotificationManager)mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                        notificationChannel.setDescription("channel description");
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.GREEN);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
                        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                        notificationManager.createNotificationChannel(notificationChannel);
                        notificationManager.notify(1,builder.build());

                    }

                    //빌리기를 누르자마자 바로 비활성화
                    //  책의 haver를 주인에서 나로 바꿔주자
                    db.collection("Books")
                            .whereEqualTo("title",title.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("AdapterBook", document.getId() + " => " + document.getData());
                                            db.collection("Books").document(document.getId())
                                                    .update(
                                                            "abled", false,
                                                            "haver",id,
                                                            "time",TIME,
                                                            "place",PLACE
                                                    );

                                        }
                                    } else {
                                        Log.d("AdapterBook", "Error getting documents: ", task.getException());
                                    }
                                }
                            });

                    //해당 아이템 리사이클러에서 삭제
                    final int adapterPosition = getAdapterPosition();
                    bookDataSet.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition,bookDataSet.size());

                }
            });


        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterBook(ArrayList<BookData> dataset, Context context) {
        bookDataSet = dataset;
        SharedPreferences preferences = context.getSharedPreferences("pref",0);
        id = preferences.getString("ID","");
        PLACE = preferences.getString("place","");
        TIME = preferences.getString("time","");
        mcontext=context;

    }

    // Create new views
    @NonNull
    @Override
    public AdapterBook.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book,parent,false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }




    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull AdapterBook.ViewHolder holder, int position) {

//        holder.cover.setImageResource(bookDataSet.get(position).book_image);

        Glide.with(holder.itemView.getContext())
                .load(bookDataSet.get(position).getBook_image())
                .into(holder.cover);
        holder.title.setText(bookDataSet.get(position).title);
        holder.author.setText(bookDataSet.get(position).author);
        holder.publisher.setText(bookDataSet.get(position).publisher);
        holder.haver.setText(bookDataSet.get(position).haver);
        holder.place.setText(bookDataSet.get(position).place);
        holder.time.setText(bookDataSet.get(position).time);


        Log.d("AdapterBook-test-title",bookDataSet.get(position).title);
    }

    @Override
    public int getItemCount() {
        return bookDataSet.size();
    }


}




