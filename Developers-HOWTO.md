V
1. Add a new controller: A controller 


Every controller is extended by UstadBaseController.
Its a controller. It does what a controller is supposed to do.

Make a new controller for the view that extends the UstadBaseController. To go to Core -> Controller 

Controllers in Android work with context and arguments. The arguments are useful for application resume (onCreate -> When app is killed off and resumes)


Controller to have method makeControllerForView which initialises the view. 

2. Make the view.

Views are always interfaces in core. And various versions for different ports. Android port will have its own implementation of that View.

We also have UstadView that all views should extend. This has info like context (android specific), direction , etc. All handled by UstadBaseActivity.



eg: 

public interface MyView{


}


Views to have setView(View view) : only there such that we have a member view with a class type.

Views should have arguments given to it. Some conrollers may not require views. 


Some views may be asynchronous.


Fragments: Android View implementation . Its android specific. It should implement UstadBaseFragment

So we have an interface MyView -> In Android, its implementation will be Fragment (Use Android Studio to do that)

You can use the IDE to implement methods.

eg:

public class MyFragment extends UstadBaseFragment implements MyView{


}
super

The UI is now created in the onCreateView method ()

BasePointController and BasePointView are both for the screen after Login ( Literally the base point of the application)

For a fragment to use click events MyFragment implements AdapterView.OnItemClick{..



How to make a new activity 


1. Make a new Controller . Extends UstadBaseController. All controllers are in core (shared between android and J2ME)

eg: 
public class MyNewController extends UstadBaseController{

//Add a constructor. Always with context object.
// context needed in Android, but not in J2ME. So its really a blank object in J2ME.
public MyNewController(Object context){
super(context);
}


Asynccontrollers are the ones that do not have to be on the main thread. (Background activities are an example)


Remember that passing arguments to Controllers are neccesary for the app to come back to life after death. 

So they go in the Controller's constructor. Say an id for the current logged in user. This info is saved in bundle whenever that sview is opened. Thats always saved by Android on resume (really onCreate since it kills the application as a whole)

eg: public MyNewController(Object context, String classid){
}

p
So make a View first that extends UstadView . This is an interface in core and different versions in ports.

Note: The View, Activity and Fragments gets loaded first and then it talks to the controller. The view needs to know the strings and all that to make the view and populate it. 


setUIStrings() is for cross platform localisation even if the language is not present on the device.


MAKE THE ACTIVITY :
private MyNewActivity extends UstadBaseActivity implements MyNewView{
}
-extends UstadBaseActivity

-Make a new activity after contorller biz This will also make an xml to go alongside. If your view consist of fragments, there will be additional xmls for every fragments.

-Add a variable for the corresponding controller: private MyNewController mController;

-The onCreate bit of the Activity is where Android will give the arguments to create (either new or ressurect from dead)

protected void onCreate(Bundle savedInstanceState){
...
}

-Import the widgets (Button, etc) you added to the xml into the Activity. 
- Since the Activity implements View, you will implement the View's methods (that Override View's) and set labels, etc from here with a direct link to the design of the Activity.

How to make button do

Make a handle Clinck button method

public void handleClickAwesomeButton(){
  //set hashtable 
  //go do something and add it as arguments to the UstadMobileSystemImpl.go method.
  //This go method goes to the other activity
}





