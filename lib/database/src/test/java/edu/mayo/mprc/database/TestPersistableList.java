package edu.mayo.mprc.database;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test of persistable lists (who are actually bags)
 *
 * @author Roman Zenka
 */
public class TestPersistableList {
    @Test
    public void emptyListsAreEqual() {
        ObjectList list1 = new ObjectList();
        ObjectList list2 = new ObjectList();
        Assert.assertEquals(list1, list2, "Empty lists must be equal");
    }

    @Test
    public void orderDoesNotMatter() {
        TestPersistable t1 = new TestPersistable(1);
        TestPersistable t2 = new TestPersistable(2);

        ObjectList list1 = new ObjectList();
        list1.add(t1);
        list1.add(t2);

        ObjectList list2 = new ObjectList();
        list2.add(t2);
        list2.add(t1);

        Assert.assertTrue(list1.equals(list2), "Order of elements does not matter");
    }

    @Test
    public void differentCollectionsAreOk() {
        TestPersistable t1 = new TestPersistable(1);
        TestPersistable t2 = new TestPersistable(2);

        ObjectList list1 = new ObjectList();
        list1.add(t1);
        list1.add(t2);

        ObjectList list2 = new ObjectList();
        List<TestPersistable> myList = new ArrayList<TestPersistable>(2);
        myList.add(t2);
        myList.add(t1);
        list2.setList(myList);

        Assert.assertTrue(list1.equals(list2), "We can use different implementation of a list and still get equality");
    }


    private class TestPersistable extends PersistableBase {
        private int value;

        private TestPersistable(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestPersistable that = (TestPersistable) o;

            if (value != that.value) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    private class ObjectList extends PersistableListBase<TestPersistable> {
        private ObjectList() {
        }

        private ObjectList(int initialCapacity) {
            super(initialCapacity);
        }

        private ObjectList(Collection<TestPersistable> items) {
            super(items);
        }
    }

}
