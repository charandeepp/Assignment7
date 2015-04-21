

/**
 * Created by Charandeep on 4/21/15.
 */


import java.util.Hashtable;
import java.net.URL;

public class Node implements ChordInterface{

    private Hashtable<String,String> dictionary_;
    private Hashtable<Integer,Hashtable<Integer,URL>> fingerTable_;


    public Node(){

        dictionary_ = new Hashtable<String,String>();
        fingerTable_ = new Hashtable<Integer,Hashtable<Integer,URL>>();
    }

    @Override
    public JoinResponse join(URL newNode){

        return null;
    }

    @Override
    public String lookup(String word){
        String meaning = null;

        return meaning;
    }

    @Override
    public URL successor() {
        return null;
    }

    @Override
    public void join_done(URL newNode) {

    }

    @Override
    public void insertKey() {

    }

    public void setDictionary_(Hashtable dictionary_) {
        this.dictionary_ = dictionary_;
    }


    public Hashtable getDictionary_() {
        return dictionary_;
    }


}
