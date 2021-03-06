
/*
 * Data structure for Social.users: -Nested HashMap
 *
 * users --> detail --> id --> content
 *
 * (root) users
 *         | (String) Name
 *                 | (String) <id>
 *                         | (String) <name>
 *         | (String) Info
 *                 | (String) <id>
 *                         | - (String) <info>
 *         | (String) Pillar
 *                 | (String) <id>
 *                         | - (String) <pillar>
 *         | (String) FifthRow
 *                 | (String) <id>
 *                         | - (String) <pillar>
 *         | (String) Telegram
 *                 | (String) <id>
 *                         | - (String) <pillar>
 *         | (String) DisplayPic
 *                 | (String) <id>
 *                         | - (String) <Storage image url>
 *         | (String) Skills
 *                 | (String) <id>
 *                         | - (String) <skill>
 *                                   | - (Long) <level>
 *
 * Access methods: -static so you call it by Social.<method>
 *      getName() - returns HashMap<id, name> (same for the other details)
 *      getName(<id>) - returns <name> (same for the other details)
 *      getUser(<id>) - returns the User class, representing a user profile
 *      getUsers() - returns ArrayList<User> of all users in db
 *      setAttr(<detail>, <id>, <value>) - write to firebase and local copy
 *                                         (value can be either String (name, info, pillar) or HashMap<String, Long> (skills))
 *      addImage(<id>, byte[] <image>) - uploads the image onto firebase storage and stores the filepath in firebase under DisplayPic
 *      displayImage(<Activity>, <url string>, <ImageView>) - displays the image given in the url onto the ImageView
 *      addUser(<name>, <info>, <pillar>, <FifthRow>, <Telegram>, <skills>) - add user to firebase, returns the <id> of the created user
 *      rmUser(<id>) - remove user from firebase
 *
 * More information on how image works:
 *      addUser: puts empty url to firebase automatically as user has not uploaded an image
 *      addImage: when user uploads an image, the image will be stored to Storage, with the filename as <id>.jpg
 *                the url to the storage image will be kept at firebase DisplayPic
 *      displayImage: sample usage - displayImage(this, "<url here>", findViewById(R.id.ImageViewIdHere))
 *                    first parameter is to specify the Activity that the image would be (MainActivity.this etc)
 *                    ways to get url: User.displayPic || Social.getDisplayPic(id)
 *
 * Sample codes:
 *
 * HashMap<String, String> name = Social.getName();
 * HashMap<String, String> info = Social.getInfo();
 * HashMap<String, String> pillar = Social.getPillar();
 * HashMap<String, HashMap<String, Long>> skills = Social.getSkills();
 *
 * Sample code to query for all users:
 *    for (String id: name.keySet()) {
 *     name.get(id);
 *     info.get(id);
 *     pillar.get(id);
 *
 *     for (Map.Entry<String, Long> entry: skills.get(id).entrySet()) {
 *         String skill = entry.getKey();
 *         Long level = entry.getValue();
 *     }
 * }
 *
 */

package com.example.sutd_social.firebase;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.sutd_social.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Social {
    private static final String TAG = "SocialSingleton";
    private static final HashSet<String> strHeader = new HashSet<>(Arrays.asList("Name", "Info", "Pillar", "FifthRow", "Telegram", "DisplayPic"));
    private static final HashSet<String> nestedHeader = new HashSet<>(Arrays.asList("Skills"));
    private static final HashMap<String, HashMap> users = new HashMap<>();
    private static DatabaseReference socialRef; // Firebase
    private static StorageReference socialImgRef; // Storage
    private static final Social ourInstance = new Social();

    public static Social getInstance() {
        return ourInstance;
    }

    private Social() {
        // Query from Firebase and get the required information
        Log.d(TAG, "Initialising Social");

        // Initialise users
        for (String i: strHeader) {
            users.put(i, new HashMap<String, String>());
        }
        users.put("Skills", new HashMap<String, HashMap<String, Long>>());

        // Set up Storage Instance
        socialImgRef = FirebaseStorage.getInstance().getReference();
        socialImgRef = socialImgRef.child("/DisplayPic");

        // Getting Firebase Instance and reference to "Social" node
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        // Enable offline capabilities - cache and disk storage
        socialRef = database.getReference("Social");
        socialRef.keepSynced(true);

        //Adding eventListener to reference
        socialRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Getting the string value of that node
                Log.d(TAG, "Updating Social.users");

                for (String detail : strHeader) {
                    users.put(detail, (HashMap<String, String>) dataSnapshot.child(detail).getValue());
                }
                users.put("Skills", (HashMap<String, HashMap<String, Long>>) dataSnapshot.child("Skills").getValue());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: Something went wrong! Error:" + databaseError.getMessage());
            }
        });

    }


    public static void setAttr(String detail, String id, String value) {
        // Overloaded method for Name, Info, Pillar (string)
        Log.d(TAG, "Writing data: " + detail);
        if (strHeader.contains(detail)) {
            socialRef.child(detail).child(id).setValue(value);

            users.get(detail).put(id, value);

        } else {
            throw new IllegalArgumentException("detail can only be: " + strHeader.toString());
        }
    }

    public static void setAttr(String detail, String id, HashMap<String, Long> skills) {
        // Overloaded method for Skills (nested long)
        Log.d(TAG, "Writing data: " + detail);
        if (nestedHeader.contains(detail)) {
            for (Map.Entry<String, Long> skill : skills.entrySet()) {
                socialRef.child(detail).child(id).child(skill.getKey()).setValue(skill.getValue());
            }

            // This sync the local copy first
            // Might be risky if the write is not successful
            users.get(detail).put(id, skills);
        } else {
            throw new IllegalArgumentException("detail can only be: Skills");
        }

    }

    //uploads the person image
    public static void addImage(final String id, Uri image) {
        // https://firebase.google.com/docs/storage/android/upload-files
        // Point to the person's image in jpg format
        String fileId = id + ".jpg";

        // Get Google Storage reference
        final StorageReference userdpRef = socialImgRef.child(fileId);
        Log.d(TAG, "Writing data: " + fileId);

        // Upload image
        UploadTask uploadTask = userdpRef.putFile(image);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                userdpRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri imageUrl = uri;

                        // Upload url to firebase
                        Social.setAttr("DisplayPic", id, imageUrl.toString());
                    }
                });
            }
        });
    }

    public static String addUser(String id, String name, String info, String pillar, String fifthRow, String telegram, HashMap<String, Long> skills) {
        // Get uuid
        // Add attribute
        setAttr("Name", id, name);
        setAttr("Info", id, info);
        setAttr("Pillar", id, pillar);
        setAttr("FifthRow", id, fifthRow);
        setAttr("Telegram", id, telegram);
        setAttr("DisplayPic", id, "");
        setAttr("Skills", id, skills);

        Log.d(TAG, "Done adding new user");
        return id;
    }

    //keep it here
    public static void rmUser(String id) {
        // Wasteful implementation just to iterate through 2 HashSets
        HashSet<String> headers = new HashSet<>();
        headers.addAll(strHeader);
        headers.addAll(nestedHeader);
        for (String i : headers) {
            socialRef.child(i).child(id).removeValue();
            users.get(i).remove(id);
        }

        // Remove dp from storage
        socialImgRef.child(id).delete();
    }

    public static HashMap<String, String> getName() {
        return users.get("Name");
    }

    public static HashMap<String, String> getInfo() {
        return users.get("Info");
    }

    public static HashMap<String, String> getPillar() {
        return users.get("Pillar");
    }

    public static HashMap<String, String> getFifthRow() {
        return users.get("FifthRow");
    }

    public static HashMap<String, String> getTelegram() {
        return users.get("Telegram");
    }

    public static HashMap<String, String> getDisplayPic() {
        return users.get("DisplayPic");
    }

    public static HashMap<String, HashMap<String, Long>> getSkills() {
        return users.get("Skills");
    }

    public static String getName(String id) {
        if (users.get("Name").containsKey(id)) {
            return (String) users.get("Name").get(id);
        }
        return null;
    }

    public static String getInfo(String id) {
        if (users.get("Info").containsKey(id)) {
            return (String) users.get("Info").get(id);
        }
        return null;
    }

    public static String getPillar(String id) {
        if (users.get("Pillar").containsKey(id)) {
            return (String) users.get("Pillar").get(id);
        }
        return null;
    }

    public static String getFifthRow(String id) {
        if (users.get("FifthRow").containsKey(id)) {
            return (String) users.get("FifthRow").get(id);
        }
        return null;
    }

    public static String getTelegram(String id) {
        if (users.get("Telegram").containsKey(id)) {
            return (String) users.get("Telegram").get(id);
        }
        return null;
    }

    public static String getDisplayPic(String id) {
        if (users.get("DisplayPic").containsKey(id)) {
            return (String) users.get("DisplayPic").get(id);
        }
        return null;
    }

    public static void displayImage(Activity context, String url, ImageView imageView) {
        Glide.with(context).load(url).placeholder(R.drawable.default_user).into(imageView);
    }

    public static HashMap<String, Long> getSkills(String id) {
        if (users.get("Skills").containsKey(id)) {
            return (HashMap<String, Long>) users.get("Skills").get(id);
        }
        return null;
    }

    public static User getUser(String id) {
        String name = getName(id);
        String info = getInfo(id);
        String pillar = getPillar(id);
        String fifthRow = getFifthRow(id);
        String telegram = getTelegram(id);
        String displayPic = getDisplayPic(id);
        HashMap<String, Long> skills = getSkills(id);

        User user = new User(name, info, pillar, fifthRow, telegram, displayPic, skills);
        return user;
    }

    public static ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();

        for (String id : Social.getName().keySet()) {
            users.add(getUser(id));
        }

        return users;
    }
}

