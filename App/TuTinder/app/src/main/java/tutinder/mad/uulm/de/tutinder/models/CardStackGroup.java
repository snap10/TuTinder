package tutinder.mad.uulm.de.tutinder.models;

import java.util.List;

/**
 * Created by Lukas on 26.06.2016.
 */
public class CardStackGroup extends CardStackObject {

    private Group group;

    public CardStackGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
