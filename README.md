<!--
# SUTD-Social
50.001 1D Android App

## Program Structure

Classes name are the same just without spaces

#### Login and Signup
- Front End
  - Sign Up
  - Log In
- Back End
  - Fetch Account Data
  - Post Account Data

#### Matching System
- Front End
  - Matching Layout
    - Search Bar
    - Matching Card 
- Back End
  - Fetched Card Data
  - Search Functionality

#### Bulletin System
- Front End
  - Bulletin Layout
  - Bulletin Create Button
  - Bulletin Card
- Card Popup
- Back End
  - Fetch Data for card
  - Post Data when creating card


## Project Split

[Use MS Planner to update what you are doing](https://tasks.office.com/sutd.edu.sg/Home/PlanViews/3W5Q1Wd7N0WEQOoRp2s7cMgABEjJ?Type=PlanLink&Channel=Link&CreatedTime=637391868765980000)

| Name | Task |
| :---: | :--- |
| Darren | Find Help Layout, bulletin layout, Bulletin create button |
|Melody | Account Creation, matching backend, bulletin backend, login |
|Kai Xun | Find help cards, Bulletin cards, Bulletin popup card |
|Le Xuan | Send help interface, Bulletin create button, Navbar|
|Jun Kai | Search Functionality, Selector/Filter (bulletin)|


| Main Component | Sub Component |
| :---: | :---: |
App | Account Creation, Nav Bar (bottom bar), Login
Help | Find Help Layout, Find Help Cards, Send Help Interface, Search Functionality, Matching Backend
Bulletin | Selector/Filter (Bulletin), Bulletin Layout , Bulletin Cards, Bulletin Create Button, Bulletin Backend


## Figma Design

<insert images here>
<insert links here too>
  

## Notes

- Use single navbar fragment thingy
- Every fragment should see the bottom navbar, except when in the bulletin
-->

# SUTD-Social

## Background
In the midst of the COVID-19 pandemic, it is hard to conduct physical meetups especially for project meetings, especially for all the courses in SUTD, which are project-intensive. It is also hard to meet new peers for networking, and some students may face trouble finding the necessary help when needed.

Hence, SUTD Social places our minimum viable product on three core features:
1. Multi-purpose Bulletin Board to refine users' personal newsfeeds to customise what they want to view.
2. Matching Algorithm allows users to seek out suitable people willing to provide help in specific areas. 
3. Profile Page allows users to fill in their skillsets and their corresponding skill levels. This creates the networking community needed to match users.

## System Design and Implementation
We connected the database to the front ends by their user ids.

### Design patterns applied 
#### 1. Object-oriented - Singleton 

Static factory method allows users to modify the instance of the class
```
public class Social {
    private static final Social ourInstance = new Social();

    public static Social getInstance() {
        return ourInstance;
    }

    private Social() {
        ...
    }
    ...
}
```

#### 2. Adapter Design Pattern
The Adapter Design Pattern is used in conjunction with the RecyclerView widget. The adapter class here is used to convert any interface inherited by any included classes into client classes that the RecyclerView ViewGroup expects. More on how the RecyclerView is used is included further down.

### Fragments

We utilised a Bottom Navigation Bar to handle switching of tabs between our app pages.

In the code below, everytime the user changes tabs via the navigation bar, we create a new instance of the corresponding fragment, and then display it on screen.

```
bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_account:
                        openFragment(new AccountFragment());
                        return true;

                    case R.id.action_match:
                        openFragment(new MatchFragment());
                        return true;

                    case R.id.action_bulletin:
                        openFragment(new BulletinFragment());
                        return true;
                }
                return false;
            }
        });

 void openFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
```
### RecyclerView
The RecyclerView widget was used to act as the list container in both the Find-Help portion and the Bulletin Board portion of our application. This was done by creating two adapter classes for both portions. They are namely the "FindHelpRecViewAdapter" and the "BulletinBoardPostRecViewAdapter" class respectively. Since they are both adapter classes for the RecyclerView widget, both classes would inherit methods from RecyclerView class.

#### 1. Find-Help RecyclerView
<center>
    <img src="http://i.imgur.com/pCR8HGV.png" width="300" height="600" />
</center>
<br />

The Find-Help portion of our application is created using a RecyclerView with the "FindHelpRecViewAdapter" class acting as the adapter.
The "FindHelpRecViewAdapter" contains an array of class "Find_Help" that we have created to store individualized information specific to that class instantiation.
The "Find_Help" class contains 5 private attributes:
1. String name
2. String pillar
3. String profile Picture
4. double confidence_level
5. String skills

Every instance of the class would be populated using information from every individual available in the firebase. An arraylist would then be used to contain every user's information, which would then be used to populate every individual card of the RecyclerView. (Code shown in Fig. 1)

##### 1.1 Search function
The search function is incorporated into the RecyclerView by first taking the inputted text from the search bar when the search button is clicked. The text would be the specific skill that users are looking out for. The inputted skill would then be used to compare with the skills of every user. Everytime a match is found, that user's information would be used to populate the "Find_Help" class, which would then be fed into the created arraylist to populate the cards in the RecyclerView.

```
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchBar.getText().toString().isEmpty()){
                    Toast.makeText(getContext(),"Please input a skill", Toast.LENGTH_SHORT).show();
                }
                else{
                    find_help_posts.clear();
                    String search_txt = searchBar.getText().toString().toLowerCase(); //lower case to account for all typing
                    //Matching Algo
                    MatchingAlgo matchingAlgo = new MatchingAlgo();
                    ArrayList<Map.Entry<String, Double>> algo_arr =  matchingAlgo.skillsIsAllSmallCaps(Admin.getUserid(),search_txt);
                    for(Map.Entry<String, Double> algo_entry : algo_arr){
                        String current_id = algo_entry.getKey();
                        Double confidence_lvl = algo_entry.getValue();
                        String total_skills = "";

                        for(String all_skill : Social.getSkills(current_id).keySet()){
                            total_skills = total_skills + all_skill + "\n";
                        }


                        find_help_posts.add(new Find_Help(Social.getName(current_id), Social.getPillar(current_id), confidence_lvl,total_skills));

                    }
                }
                
                adapter.setPosts(find_help_posts);
                find_help_RecView.setAdapter(adapter);
            }
        });
```

#### 2. Bulletin Board RecyclerView
<center>
<img src="https://i.imgur.com/tyhCmcU.png" width="300" height="600" />
</center>
<br />

The Bulletin Board is created using a RecyclerView with the "BulletinBoardPostRecViewAdapter" class acting as the adapter.
The "BulletinBoardPostRecViewAdapter" contains an array of class "BulletinBoardPost" that we have created to store individualized information specific to every Bulletin Board post. 
The "BulletinBoardPost" class contains 4 private attributes:
1. String postTitle
2. String postDescription
3. String postdate
4. String bulletin_url

Every instance of the class would be populated using information from each post data available in the firebase. An arraylist would then be used to contain every user's information, which would then be used to populate every individual card of the RecyclerView. (Code shown in Figure 2)

##### 2.1 Adding new posts
##### 2.1.1 Floating Action Button
<center>
<img src="https://i.imgur.com/ctTwQzO.png" />
</center>
<br />

A floating action button was put on the Bulletin Fragment to allow users to add new posts to the bulletin board. Upon clicking the button, the user would be directed to another activity. This done through the popup window method where a smaller activity is created with a smaller width and height to allow the previous activity to be seen in the background. (Code shown in Fig. 2.1)

##### 2.1.2 Popup Window
<center>
<img src="https://i.imgur.com/L8bUoe5.png" width="300" height="600" />
</center>
<br />

Within the popup window, users are required to include 4 things:
1. A title
2. A date
3. A description
4. A photo

When the confirm button is clicked, the local arraylist would be populated with the new "BulletinBoardPost" class to create a new post. In addition, this new post is also added into our firebase to store the new information.
On the other hand, all added information would be discarded when the cancel button is clicked.



In addition, the manifest file is also adjusted to add a new style for the popup activity. 
```
<activity
    android:name="com.example.sutd_social.BulletinPopUp"
    android:theme="@style/AppTheme.Popme" ></activity>
```
This style gives 2 new attributes to the popup window:
1. Background is translucent (To show part of the previous activity)
2. Current activity is closed when the outside portion is clicked.

```
<style name="AppTheme.Popme">
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowCloseOnTouchOutside">true</item>
```
#### 2.2 Individual post popup
<center>
<img src="https://i.imgur.com/ySlxRlG.png" width="300" height="600" />
</center>
<br />

Upon clicking on an individual post in the bulletin board, a popup specific to that post would be executed. This would allow users to get a better view on all details relevant to that post.
This is done in a similar way to the previous popup method by using a popup window with the same style and attributes. (Code shown in Fig. 2.2)

### Firebase Integration
To add data synchronisation between user applications, we decided to use Firebase to store the user's data so all users will be able to receive real-time updates on the app. To do that, we did up all the utility methods for accessing firebase into 2 classes, `Social` and `BulletinBoard` for find-help and bulletin respectively. In each of the class, we have the following funtions

1. We applied the Singleton design pattern as mentioned above. In the constructor, we initialise connection to the Firebase and set up a listener to fetch updates from Firebase. 

```
private static final HashMap<String, Bulletin> bulletinBoard = new HashMap<>();

private BulletinBoard() {
    // Set up Storage Instance
    bulletinImgRef = FirebaseStorage.getInstance().getReference();
    bulletinImgRef = bulletinImgRef.child("/BulletinBoardPic");

    //Getting Firebase Instance
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    //Getting Reference to Root Node
    bulletinRef = database.getReference();

    //Getting reference to "BulletinBoard" node
    bulletinRef = bulletinRef.child("BulletinBoard");

    //Adding eventListener to reference
    bulletinRef.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d(TAG, "onChildAdded: Adding " + snapshot.getKey());
            bulletinBoard.put(snapshot.getKey(), snapshot.getValue(Bulletin.class));
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            // Same as onChildAdded
            Log.d(TAG, "onChildChanged: " + snapshot.getKey());
            bulletinBoard.put(snapshot.getKey(), snapshot.getValue(Bulletin.class));
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            bulletinBoard.remove(snapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.e(TAG, "onChildMoved: " + snapshot.getKey() + snapshot.getValue(Bulletin.class).toString());
            Log.e(TAG, "onChildMoved: You should not be triggering this method!!");
        }

        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "onCancelled: Something went wrong! Error:" + databaseError.getMessage());
        }
    });
}
```

2. This relies on the `Bulletin` class which stores all the relevant data needed. It ensures that it is easy to integrate Firebase into the app as they would just need to know what is in a `Bulletin` class and not how the firebase methods.

```
public class Bulletin {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // use this with format() or parse()
    public String title;
    public String description;
    public String fifthRow;
    public String image;
    public String url;
    public String expiryDate;

    Bulletin() {
    }

    Bulletin(String title, String description, String fifthRow, String image, String url, String expiryDate) {
        this.fifthRow = fifthRow;
        this.title = title;
        this.description = description;
        this.image = image;
        this.url = url;
        this.expiryDate = expiryDate;
    }

    ... more constructors below...
}
```

3. Users can access and add new entries using the following static methods.
```
// Get the whole HashMap of Bulletins
public static HashMap<String, Bulletin> getBulletin() {
    return bulletinBoard;
}

// Get the Bulletin with the specified id
public static Bulletin getBulletin(String id) {
    return bulletinBoard.get(id);
}

// Use this to add new Bulletin or update Bulletin
public static void addBulletin(String id, Bulletin bulletin) {
    bulletinRef.child(id).setValue(bulletin);
    bulletinBoard.put(id, bulletin);  // TODO: unsafe operation. Add failure listener
}

// Alternatively:
public static void addBulletin(String id, String title, String description, String fifthRow, String image, String url, String expiryDate) {
    // If the user never specify any field, the default is "" (same goes for image)
    Bulletin newBulletin = new Bulletin(title, description, fifthRow, image, url, expiryDate);

    addBulletin(id, newBulletin);
}

public static void addBulletin(String id, String title, String description, String fifthRow, Uri image, String url, String expiryDate) {
    // Similar method as above, except that image is provided
    Bulletin bulletin = new Bulletin(title, description, fifthRow, "", url, expiryDate);
    addImage(id, bulletin, image);
}
```

### Displaying of Images
To add storage and display of user profile/bulletin images, we did the following steps:

1. Get the images from the user using an implicit intent:

```
// Upon button press, trigger intent
Intent openGallery = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
startActivityForResult(openGallery, 10);

// Get data and trigger Social.addImage() method
@Override
public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 10) {
        if (resultCode == getActivity().RESULT_OK) {
            Uri imageUri = data.getData();
            Social.addImage(Admin.getUserid(), imageUri);
        }
    }
}
```
2. Attempt to upload the image onto Google Cloud Store and store the file path as a url in Firebase.
```
// addImage() method can be found in Social and BulletinBoard classes
public static void addImage(final String id, Uri image) {
    // https://firebase.google.com/docs/storage/android/upload-files
    // Point to the person's image in jpg format
    String fileId = id + ".jpg";
    
    // Get Google Storage reference
    final StorageReference userdpRef = socialImgRef.child(fileId);

    // Upload image
    UploadTask uploadTask = userdpRef.putFile(image);
    uploadTask.addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            // Unsuccessful uploads
        }
    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            // Success!
            // Get file url to upload url onto firebase
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
```
3. Display image with static funciton `displayImage()` from Social or BulletinBoard classes
```
public static void displayImage(Activity context, String url, ImageView imageView) {
    // Get Activity via this or getActivity()
    // Get URL using getDisplayPic()
    // Get ImageView via findViewById()
    Glide.with(context).load(url).placeholder(R.drawable.default_user).into(imageView);
}
```

### Special feature: Matching algorithm
Metrics considered:
1. Number of skillsets the individual user has.
2. Input confidence score for each skill chip makes it more quantifiable.

We obtained each user data from the `Social` class which connects to the firebase.

Calculations done: 
1. Average out the confidence level of each user in the user database 

```
    public long stats_bp(long[] confidence) {
        for (int a = 0; a < confidence.length; a++) {
            meanUserconf += confidence[a];
        }
        meanUserconf = meanUserconf / confidence.length;
        return meanUserconf;
    }
```
2. Average out the number of skillsets of each user in the database for a fairer comparison

```
    public long avgNumSkillsets() {
        //numId is the total number of users
        //query social.java
        int numId = Social.getName().size();
        int numOfSkills = 0;
        for (HashMap<String, Long> skill : skillset.values()) {
            numOfSkills += skill.size();
        }
        return numOfSkills / numId;
    }
```
3. Take the difference between user confidence score and the average confidence score. Same goes for the skillsets 

4. Do the euclidean distance for the two metrics above to generate the euclidean distance to generate higher suitability. This is to also prevent users from stating their overconfidence.
```
    public double generateEuclideanDistance(HashMap<String, HashMap<String, Long>> skillset, String skill, String id) {
        long x_dist = getUserskillLevel(skillset, skill, id) - meanUserconf;
        long y_dist = getNumSkillsets(id) - avgNumSkillsets();
        match = Math.hypot((x_dist), (y_dist));
        return match;
    }
```

```
   public HashMap<String, Double> percentageGenerated(HashMap<String, HashMap<String, Long>> skillset, String skill) {
        HashMap<String, Double> percent = new HashMap();
        for (String id : skillset.get(skill).keySet()) {
            double distCompare = generateEuclideanDistance(skillset, skill, id);
            percent.put(id, distCompare);
        }

        Double min = Collections.min(percent.values());
        Double max = Collections.max(percent.values());

        for (Map.Entry<String, Double> entry : percent.entrySet()) {
            if (max == min || entry.getValue() == min) {
                percent.put(entry.getKey(), entry.getValue());
            } else {
                double p = ((entry.getValue() - min) / (max - min)) * 100;
                percent.put(entry.getKey(), p);
            }}
        return percent;

    }
```
5. **Comparator interface implemented (Generics)** to sort the confidence levels. Used a hashmap to tag the calculated skill level to the student id.
```
    //final return
    public ArrayList<Map.Entry<String, Double>> sortId(HashMap<String, Double> percent) {
        ArrayList<Map.Entry<String, Double>> list = new ArrayList<>(percent.entrySet());
        Log.i(TAG, "ArrayList is done");
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        return list;
    }
```


## Task Delegation


| Task                                                   | People in charge |
| ------------------------------------------------------ | ---------------- |
| Cards for Matching page and Bulletin Board and Bulletin Board mockup   | Kai Xun          |
| UI development, integration between front-end & back-end and search function                            | Darren           |
| Profile page with chips functionality, Navbar Fragments    | Le Xuan          |
| Matching algorithm, Login and Signup authentication, design mockups and project timeline                                                    |         Melody         |
| Firebase authentication, classes in Firebase package, creation of users, displaying images, backend debugger| Jun Kai          |

## UI and UX designing 
Our group planned some core mockups on our figma and stuck to a specific colour combi of cool colours which appeals to the majority of the SUTD population. Refer to our [Mockup](https://www.figma.com/file/uZD4hLOV5jlTJceJGMKUeY/50.001-1D?node-id=17%3A6) for some of the wireframes.
## Possible Future Work 
In order to flesh out the application more, features such as adding accounts as friends, or having more interactions with the bulletin posts could be added. The scale of our project was targetted to be the SUTD campus, hence, useful features that other SUTDents might request could also be added. There are also user testing that need to be done to ensure that our application will be production ready for use. However, core functionalities of our application has already been implemented during the 1D project, hence the remaining work is mainly bug fixes, optimization of the algorithm and realtime database and fine tune the user interface.

## Conclusion
We set out to create a social media application for SUTD students. We explored certain social issues within SUTD and set out to solve it with SUTD Social. Our application aims to do this with its 2 main features, the bulletin board and the find help feature. 

## Appendix

### Figure 1 (Find-Help RecyclerView)
```
public class FindHelpRecViewAdapter extends RecyclerView.Adapter<FindHelpRecViewAdapter.ViewHolder> {

    private ArrayList<Find_Help> find_help_posts = new ArrayList<>(); // create new ArrayList to fit into Recycler View

    private final Context context;  // context have to be created in order for items to reference it

    public FindHelpRecViewAdapter(Context context) {  // create constructor for Adapter Class
        this.context = context;   // constructor for context
    }

    @NonNull
    @Override                // ViewGroup is parent of all layout files and is used to group different views inside it
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  // this generates the view holder
        View view = LayoutInflater.from(parent.getContext()) //this inflates the view and requires context from ViewGroup parent
                .inflate(R.layout.find_help_posts, parent, false);  // in inflate need 3 arguements, 1st is address of layout of every item in recyclerView, 2nd is ViewGroup (Where you want to attach View object, if Main Activity layout is Relative, parent is also relative), 3rd is a boolean

        ViewHolder holder = new ViewHolder(view);
        return holder;                                                                                                                               // boolean is false when parent is known and true when parent is changed to null which shows that you do not know where the parent is located at
    }

    @Override   // on Click Listener is set here   , final added as position used in inner class
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {   //position is position of item inside recyclerView
    
        holder.StudentName.setText(find_help_posts.get(position).getName());
        holder.StudentPillar.setText(find_help_posts.get(position).getPillar());
        holder.Skills.setText(find_help_posts.get(position).getSkills());
        Social.displayImage((Activity)context, find_help_posts.get(position).getProfilePicture(),holder.ProfilePicture);


    }

    @Override
    public int getItemCount() {
        return find_help_posts.size();
    } // returns count of data, number of items inside adapter

    public void setPosts(ArrayList<Find_Help> posts) {  // create setter for ArrayList as it is private and this can be used to allow access when in Main
        this.find_help_posts = posts;
        notifyDataSetChanged();  // This is to notify Recycler View to refresh when ArrayList is changed from the internet
    }

    public class ViewHolder extends RecyclerView.ViewHolder{   // inner view holder class will hold view for every item in recycler view

        private final CardView cardView;  // initiallize all widgets that were inside the new layout file
        private final TextView StudentName;
        private final TextView StudentPillar;
        private TextView MatchRate;
        private TextView Skills;
        private final ImageView ProfilePicture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);  // Have to initialize findViewByID through the view object in this class
            ProfilePicture = itemView.findViewById(R.id.cardProfilePicture);
            StudentName = itemView.findViewById(R.id.cardStudentName);
            StudentPillar = itemView.findViewById(R.id.cardStudentPillar);
            MatchRate = itemView.findViewById(R.id.cardMatchRate);
            Skills = itemView.findViewById(R.id.cardTagList);
            //parent = itemView.findViewById(R.id.parent);  // Have to instantiate to access from OnBindViewHolder Method
        }
    }
}
```

### Figure 2 (Bulletin Board RecyclerView)
```
public class BulletinBoardPostRecViewAdapter extends RecyclerView.Adapter<BulletinBoardPostRecViewAdapter.ViewHolder>{ // this means that datatype of Adapter is the viewHolder Class

    private ArrayList<BulletinBoardPost> posts = new ArrayList<>(); // create new ArrayList to fit into Recycler View

    private final Context context;  // context have to be created in order for items to reference it
    private final int activity_code_inner = 2;

    public BulletinBoardPostRecViewAdapter(Context context) {  // create constructor for Adapter Class
        this.context = context;   // constructor for context
    }

    @NonNull
    @Override                // ViewGroup is parent of all layout files and is used to group different views inside it
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  // this generates the view holder
        View view = LayoutInflater.from(parent.getContext()) //this inflates the view and requires context from ViewGroup parent
                .inflate(R.layout.bulletin_board_post, parent, false);  // in inflate need 3 arguements, 1st is address of layout of every item in recyclerView, 2nd is ViewGroup (Where you want to attach View object, if Main Activity layout is Relative, parent is also relative), 3rd is a boolean

        ViewHolder holder = new ViewHolder(view);
        return holder;                                                                                                                               // boolean is false when parent is known and true when parent is changed to null which shows that you do not know where the parent is located at
    }

    @Override   // on Click Listener is set here   , final added as position used in inner class
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {   //position is position of item inside recyclerView

        holder.postTitle.setText(posts.get(position).getPostTitle());
        holder.postDescription.setText(posts.get(position).getPostDescription());
        //adding image
        BulletinBoard.displayImage((Activity) context, posts.get(position).getBulletin_url(),holder.cardImage);
        String inside_url = posts.get(position).getBulletin_url();


        holder.BulletinBoardParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity origin = (Activity) context;

                Intent inner_intent = new Intent(context,Bulletin_inner_post_popup.class);
                inner_intent.putExtra("inner_title", posts.get(position).getPostTitle().toString());
                inner_intent.putExtra("inner_description", posts.get(position).getPostDescription().toString());
                inner_intent.putExtra("inner_picture", inside_url);
                origin.startActivityForResult(inner_intent,activity_code_inner);

            }
        });


    }

    @Override
    public int getItemCount() {
        return posts.size();
    } // returns count of data, number of items inside adapter

    public void setPosts(ArrayList<BulletinBoardPost> posts) {  // create setter for ArrayList as it is private and this can be used to allow access when in Main
        this.posts = posts;
        notifyDataSetChanged();  // This is to notify Recycler View to refresh when ArrayList is changed from the internet
    }

    public class ViewHolder extends RecyclerView.ViewHolder{   // inner view holder class will hold view for every item in recycler view

        private final TextView postTitle;
        private final TextView postDescription;  // initiallize all widgets that were inside the new layout file
        private final CardView BulletinBoardParent;
        private ImageView cardImage;
        private TextView title_inner;
        private TextView description_inner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.card_title);  // Have to initialize findViewByID through the view object in this class
            postDescription = itemView.findViewById(R.id.card_content);
            BulletinBoardParent = itemView.findViewById(R.id.bulletin_board_post_layout);  // Have to instantiate to access from OnBindViewHolder Method
            cardImage = itemView.findViewById(R.id.card_image);
            title_inner = itemView.findViewById(R.id.txtView_title_inner);
            description_inner = itemView.findViewById(R.id.txtView_Description_inner);
        }
    }
}
```

### Figure 2.1 (Bulletin Board new post popup)
```
public class BulletinPopUp extends Activity {

    private Button btn_Confirm,btn_Cancel;
    private EditText edtTxtTitle;
    private EditText edtTxtDescription;
    private EditText edtTxteventdate;
    private ImageView popupImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_pop_up);

        edtTxtTitle = findViewById(R.id.edtTxt_Title);
        edtTxtDescription = findViewById(R.id.edtTxt_description);
        edtTxteventdate = findViewById(R.id.eventDate);
        btn_Confirm = findViewById(R.id.btn_Confirm);
        popupImageView = findViewById(R.id.bulletin_popup_imageview); // on click this thing jun kai here


        btn_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmCheck();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                String txtTitle = edtTxtTitle.getText().toString();
                String txtDescription = edtTxtDescription.getText().toString();
                String txtDate = edtTxteventdate.getText().toString();
                //adding to firebase
                BulletinBoard.addBulletin(Admin.getUserid(), new Bulletin(txtTitle,txtDescription));

                intent.putExtra("txtTitle", txtTitle);
                intent.putExtra("txtDescription", txtDescription);
                intent.putExtra("txtDate", txtDate);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        btn_Cancel = findViewById(R.id.btn_Cancel);
        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width *0.83), (int)(height*0.85));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

        //prompt the user by checking if the inputs are justified correctly
        private void confirmCheck(){
        String Txttitle = edtTxtTitle.getText().toString();
        if(Txttitle.isEmpty()){
            Toast.makeText(BulletinPopUp.this,"Please enter the event title!",Toast.LENGTH_LONG).show();
            return;
        }}


    }
```

### 2.2 Figure 2.2 (Bulletin Board individual post popup)
```
public class Bulletin_inner_post_popup extends Activity {

    private TextView textView_inner_title, textView_inner_description;
    private Button popUpBackButton;
    private ImageView inner_imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_inner_post_popup);

        textView_inner_title = findViewById(R.id.txtView_title_inner);
        textView_inner_description = findViewById(R.id.txtView_Description_inner);
        inner_imageView = findViewById(R.id.card_image);

        Intent intent = getIntent();

        String inner_title = intent.getStringExtra("inner_title");
        String inner_description = intent.getStringExtra("inner_description");
        String inner_picture = intent.getStringExtra("inner_picture");

        textView_inner_title.setText(inner_title);
        textView_inner_description.setText(inner_description);

        BulletinBoard.displayImage(this, inner_picture,inner_imageView);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        popUpBackButton = findViewById(R.id.card_back);
        popUpBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });

        getWindow().setLayout((int)(width *0.83), (int)(height*0.85));

        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.clear));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.3f;
        getWindow().setAttributes(params);
    }
}
```
# Code directory tree for reference: 
1. [Social.java class](https://github.com/Darren-Loh/SUTD-Social/blob/master/app/src/main/java/com/example/sutd_social/firebase/Social.java)
2. [Matching Algorithm](https://github.com/Darren-Loh/SUTD-Social/blob/master/app/src/main/java/com/example/sutd_social/firebase/MatchingAlgo.java)
