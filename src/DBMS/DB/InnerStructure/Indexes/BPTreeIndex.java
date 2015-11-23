package DBMS.DB.InnerStructure.Indexes;

/**
 * Created by litleleprikon on 22/11/15.
 * Implements a SortedMap as a B+ tree.
 */


import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Keys.Key;
import com.sun.org.apache.xpath.internal.Arg;

import java.io.StringWriter;
import java.util.*;

/**

 */
public class BPTreeIndex<K extends Comparable<K>, V> extends Index
{
    private int order;
    private int leafOrder;

    private Node root;
    private int size = 0;

    private LeafNode firstLeaf;

    private int modCount = Integer.MIN_VALUE;
    private Set<Entry<K,V>> esInstance = (new SubMap()).entrySet();

    /**
     * Creates a new BPTreeIndex.
     * @throws IllegalArgumentException thrown if order < 3 or leafOrder < 1.
     */
    public BPTreeIndex(String name, Argument argument)
    {
        super(name, argument);
        this.order = 3;
        this.leafOrder = 3;

        root = firstLeaf = new LeafNode();
    }

    /**
     * Returns the first key currently in this BPTreeIndex.
     */
    public K firstKey()
    {
        if(size == 0)
            throw new NoSuchElementException();

        return firstLeaf.keys.get(0);
    }

    /**
     * Returns the last key currently in this BPTreeIndex.
     */
    public K lastKey()
    {
        if(size == 0)
            throw new NoSuchElementException();

        LeafNode cur = firstLeaf;
        while(cur.next != null)
            cur = cur.next;

        return cur.keys.get(cur.keys.size() - 1);
    }

    /**
     * Returns the number of key-value mappings in this BPTreeIndex.
     */
    public int size()
    {
        return size;
    }

    /**
     * Removes all mappings from this BPTreeIndex.
     */
    public void clear()
    {
        root = firstLeaf = new LeafNode();
        size = 0;
        modCount++;
    }

    /**
     * Returns true if this BPTreeIndex contains a mapping for the specified key.
     */
    public boolean containsKey(K key)
    {
        Node cur = root;
        while(cur instanceof BPTreeIndex.GuideNode)
        {
            GuideNode gn = (GuideNode)cur;
            int index = findGuideIndex(gn, key);
            cur = gn.children.get(index);
        }

        LeafNode ln = (LeafNode)cur;
        return findLeafIndex(ln, key) != -1;
    }

    /**
     * Returns the value to which this BPTreeIndex maps the specified key or null if it contains no mapping for the key.
     */
    public V get(K key)
    {
        Node cur = root;
        while(cur instanceof BPTreeIndex.GuideNode)
        {
            GuideNode gn = (GuideNode)cur;
            int index = findGuideIndex(gn, key);
            cur = gn.children.get(index);
        }

        LeafNode ln = (LeafNode)cur;
        int index = findLeafIndex(ln, key);
        if(index == -1)
            return null;
        else
            return ln.values.get(index);
    }

    /**
     * Associates the specified value with the specified key in this map.
     */
    public V put(K key, V value)
    {
        if(key == null)
            throw new NullPointerException();

        // Increment size?
        if(!containsKey(key))
            size++;

        // Get previous value at the key.
        V ret = get(key);

        // Insert the new key/value into the tree.
        Node newNode = root.put(key, value);

        // Create new root?
        if(newNode != null)
        {
            GuideNode newRoot = new GuideNode();
            newRoot.keys.add(newNode.keys.get(0));
            newRoot.children.add(root);
            newRoot.children.add(newNode);

            root = newRoot;
        }

        // Increment mod count.
        modCount++;

        // Return the previous value.
        return ret;
    }

    /**
     * Removes the specified key from this map.
     */
    public V remove(K key)
    {
        if(key == null)
            throw new NullPointerException();

        // Get value of key to be removed and then remove it.
        V ret = get(key);
        if(ret != null)
        {
            root.remove(key);
            size--;
        }

        // If the root is a guide node with only one child, then set the root to that child.
        if(root instanceof BPTreeIndex.GuideNode && root.keys.size() == 1)
            root = ((GuideNode)root).children.get(0);

        // Increment mod count.
        modCount++;

        return ret;
    }

    /**
     * Returns a set view of the entries mapped in this BPTreeIndex.
     */
    public Set<Entry<K, V>> entrySet()
    {
        return esInstance;
    }

    /**
     * Returns a map representing a sub-range of the keys stored in this BPTreeIndex.
     */
    public SubMap subMap(K arg0, K arg1)
    {
        return new SubMap(arg0, arg1);
    }

    /**
     * Returns a map representing a sub-range of the keys stored in this BPTreeIndex.
     */
    public SubMap headMap(K arg0)
    {
        return subMap(null, arg0);
    }

    /**
     * Returns a map representing a sub-range of the keys stored in this BPTreeIndex.
     */
    public SubMap tailMap(K arg0)
    {
        return subMap(arg0, null);
    }

    /**
     * Returns the index to follow in a guide node for the specified key.
     */
    private int findGuideIndex(GuideNode node, K key)
    {
        for(int i = 1; i < node.keys.size(); i++)
        {
            if(key.compareTo(node.keys.get(i)) < 0)
                return i - 1;
        }

        return node.keys.size() - 1;
    }

    /**
     * Returns the index to follow in a guide node for the specified key.
     */
    private int findLeafIndex(LeafNode node, K key)
    {
        for(int i = 0; i < node.keys.size(); i++)
        {
            if(key.compareTo(node.keys.get(i)) == 0)
                return i;
        }

        return -1;
    }

    /**
     * Prints this BPTreeIndex to the specified StringWriter in XML format.
     */
    public void printXml(StringWriter out)
    {
        int cardinality = size;
        int height = 1;

        // Calc height.
        Node cur = root;
        while(cur instanceof BPTreeIndex.GuideNode)
        {
            height++;
            cur = ((GuideNode)cur).children.get(0);
        }

        // Write.
        out.write("      <bptree cardinality=\"" + cardinality + "\" height=\"" + height + "\" bpOrder=\"" + order + "\" leafOrder=\"" + leafOrder + "\">\n");
        root.printXml(out, 4);
        out.write("      </bptree>\n");
    }

    /**
     * Base class for tree nodes.
     */
    private abstract class Node
    {
        public ArrayList<K> keys;

        /**
         * Maps the specified key to the specified value in this Node.
         * @return A new right node if this node was split, else null.
         */
        public abstract Node put(K key, V value);

        /**
         * Removes the specified key from this Node.
         * @return 0 if nothing was removed, 1 if a key was removed but Nodes did not merge,
         *   2 if this Node merged with its left sibling, or 3 if this Node merged with its right sibling.
         */
        public abstract int remove(K key);

        /**
         * Prints this Node and all sub-Node to the specified StringWriter in XML format.
         */
        public abstract void printXml(StringWriter out, int indent);
    }

    /**
     * Represents a guide node in the tree.
     */
    private class GuideNode extends Node
    {
        public ArrayList<Node> children;

        public GuideNode prev = null;
        public GuideNode next = null;

        /**
         * Creates a new GuideNode of the specified order.
         */
        public GuideNode()
        {
            keys = new ArrayList<>(order);
            children = new ArrayList<>(order);

            keys.add(null); // Serves as lower-bound key.
        }

        /**
         * Maps the specified key to the specified value in this Node.
         * @return A new right node if this node was split, else null.
         */
        public Node put(K key, V value)
        {
            GuideNode newGuide = null;

            int guideIndex = findGuideIndex(key);

            // Recurse to child.
            Node newNode = children.get(guideIndex).put(key, value);

            // Did we split?
            if(newNode != null)
            {
                // Insert the new key and node at the found index.
                keys.add(guideIndex + 1, newNode.keys.get(0));
                children.add(guideIndex + 1, newNode);

                // Do we need to split?
                if(keys.size() > order)
                {
                    newGuide = new GuideNode();

                    newGuide.keys.clear();
                    newGuide.keys.addAll(keys.subList(keys.size() / 2, keys.size()));
                    newGuide.children.addAll(children.subList(children.size() / 2, children.size()));

                    ArrayList<K> newKeys = new ArrayList<>(leafOrder);
                    ArrayList<Node> newChildren = new ArrayList<>(leafOrder);

                    newKeys.addAll(keys.subList(0, keys.size() / 2));
                    newChildren.addAll(children.subList(0, children.size() / 2));

                    keys = newKeys;
                    children = newChildren;

                    newGuide.next = next;
                    newGuide.prev = this;
                    if(next != null)
                        next.prev = newGuide;
                    next = newGuide;
                }
            }

            return newGuide;
        }

        /**
         * Removes the specified key from this Node.
         * @return 0 if nothing was removed, 1 if a key was removed but Nodes did not merge,
         *   2 if this Node merged with its left sibling, or 3 if this Node merged with its right sibling.
         */
        public int remove(K key)
        {
            int guideIndex = findGuideIndex(key);

            // Recurse to child.
            int result = children.get(guideIndex).remove(key);

            // Was nothing removed?
            if(result == 0)
            {
                return 0;
            }

            // Was a key removed but no nodes were merged?
            else if(result == 1)
            {
                // It's possible that a key was moved from the left node
                // or that the index 0 key was removed, and so we need to update
                // the key for the main node.
                keys.remove(guideIndex);
                keys.add(guideIndex, children.get(guideIndex).keys.get(0));

                // It's possible that a key was moved from the right node, and so we need to update the key for that node.
                if(guideIndex + 1 < keys.size())
                {
                    keys.remove(guideIndex + 1);
                    keys.add(guideIndex + 1, children.get(guideIndex + 1).keys.get(0));
                }
            }

            // Was the child node merged with its left sibling?
            else if(result == 2)
            {
                children.remove(guideIndex);
                keys.remove(guideIndex);
            }

            // Was the child node merged with its right sibling?
            else if(result == 3)
            {
                children.remove(guideIndex + 1);
                keys.remove(guideIndex + 1);
            }

            // Are we still be above the minimum size?
            if(keys.size() >= (order + 1) / 2)
            {
                return 1;
            }

            // Otherwise, we need to check the neighbors.
            else
            {
                // Does the left node have more keys than it needs?
                if(prev != null && prev.keys.size() - 1 >= (order + 1) / 2)
                {
                    // Simply move the last key from the previous node.
                    int prevIndex = prev.keys.size() - 1;
                    K k = prev.keys.get(prevIndex);
                    Node c = prev.children.get(prevIndex);
                    prev.keys.remove(prevIndex);
                    prev.children.remove(prevIndex);
                    keys.add(0, k);
                    children.add(0, c);

                    return 1;
                }

                // Does the right node have more keys than it needs?
                else if(next != null && next.keys.size() - 1 >= (order + 1) / 2)
                {
                    // Simply move the first key from the next node.
                    K k = next.keys.get(0);
                    Node c = next.children.get(0);
                    next.keys.remove(0);
                    next.children.remove(0);
                    keys.add(k);
                    children.add(c);

                    return 1;
                }

                // Otherwise, merge with left?
                else if(prev != null)
                {
                    // We actually want to keep the left node, so add all of the keys in this node to the left node.
                    prev.keys.addAll(keys);
                    prev.children.addAll(children);
                    prev.next = next;
                    if(next != null)
                        next.prev = prev;

                    return 2;
                }

                // Otherwise, merge with right?
                else if(next != null)
                {
                    // Add all keys in right node to this node.
                    keys.addAll(next.keys);
                    children.addAll(next.children);
                    if(next.next != null)
                        next.next.prev = this;
                    next = next.next;

                    return 3;
                }

                // Otherwise, we're the root and it's okay to be less than leafOrder / 2.
                else
                {
                    return 1;
                }
            }
        }

        /**
         * Returns the guide index of to use when looking for the specified key.
         */
        private int findGuideIndex(K key)
        {
            return BPTreeIndex.this.findGuideIndex(this, key);
        }

        /**
         * Prints this Node and all sub-Node to the specified StringWriter in XML format.
         */
        public void printXml(StringWriter out, int indent)
        {
            for(int i = 0; i < indent; i++)
                out.write("  ");
            out.write("<guide>\n");

            // Print first child.
            children.get(0).printXml(out, indent + 1);

            // Print each key followed by its greater child.
            for(int i = 1; i < keys.size(); i++)
            {
                K key = keys.get(i);
                Node child = children.get(i);

                // Print key.
                for(int j = 0; j < indent + 1; j++)
                    out.write("  ");
                out.write("<key value=\"" + key.toString() + "\"/>\n");

                // Print child.
                child.printXml(out, indent + 1);
            }

            for(int i = 0; i < indent; i++)
                out.write("  ");
            out.write("</guide>\n");
        }
    }

    /**
     * Represents a leaf node in the tree.
     */
    private class LeafNode extends Node
    {
        public ArrayList<V> values;

        private LeafNode prev = null;
        private LeafNode next = null;

        /**
         * Creates a new LeafNode of the specified order.
         */
        public LeafNode()
        {
            keys = new ArrayList<>(leafOrder);
            values = new ArrayList<>(leafOrder);
        }

        /**
         * Maps the specified key to the specified value in this Node.
         * @return A new right node if this node was split, else null.
         */
        public Node put(K key, V value)
        {
            LeafNode newLeaf = null;

            // Find insert index.
            int insertIndex = 0;
            while(insertIndex < keys.size())
            {
                if(key.compareTo(keys.get(insertIndex)) <= 0)
                    break;

                insertIndex++;
            }

            // If the key already exists, then just replace.
            if(insertIndex < keys.size() && keys.get(insertIndex).equals(key))
            {
                values.set(insertIndex, value);
            }
            else
            {
                // Insert the new key and value at the found index.
                keys.add(insertIndex, key);
                values.add(insertIndex, value);

                // Do we need to split?
                if(keys.size() > leafOrder)
                {
                    newLeaf = new LeafNode();

                    newLeaf.keys.addAll(keys.subList(keys.size() / 2, keys.size()));
                    newLeaf.values.addAll(values.subList(values.size() / 2, values.size()));

                    ArrayList<K> newKeys = new ArrayList<>(leafOrder);
                    ArrayList<V> newValues = new ArrayList<>(leafOrder);

                    newKeys.addAll(keys.subList(0, keys.size() / 2));
                    newValues.addAll(values.subList(0, values.size() / 2));

                    keys = newKeys;
                    values = newValues;

                    newLeaf.next = next;
                    newLeaf.prev = this;
                    if(next != null)
                        next.prev = newLeaf;
                    next = newLeaf;
                }
            }

            return newLeaf;
        }

        /**
         * Removes the specified key from this Node.
         * @return 0 if nothing was removed, 1 if a key was removed but Nodes did not merge,
         *   2 if this Node merged with its left sibling, or 3 if this Node merged with its right sibling.
         */
        public int remove(K key)
        {
            int leafIndex = findLeafIndex(key);

            // Was the specified key not found?
            if(leafIndex == -1)
                return 0;

            // Remove the key.
            keys.remove(leafIndex);
            values.remove(leafIndex);

            // Are we still be above the minimum size?
            if(keys.size() >= (leafOrder + 1) / 2)
            {
                return 1;
            }

            // Otherwise, we need to check the neighbors.
            else
            {
                // Does the left node have more keys than it needs?
                if(prev != null && prev.keys.size() - 1 >= (leafOrder + 1) / 2)
                {
                    // Simply move the last key from the previous node.
                    int prevIndex = prev.keys.size() - 1;
                    K k = prev.keys.get(prevIndex);
                    V v = prev.values.get(prevIndex);
                    prev.keys.remove(prevIndex);
                    prev.values.remove(prevIndex);
                    keys.add(0, k);
                    values.add(0, v);

                    return 1;
                }

                // Does the right node have more keys than it needs?
                else if(next != null && next.keys.size() - 1 >= (leafOrder + 1) / 2)
                {
                    // Simply move the first key from the next node.
                    K k = next.keys.get(0);
                    V v = next.values.get(0);
                    next.keys.remove(0);
                    next.values.remove(0);
                    keys.add(k);
                    values.add(v);

                    return 1;
                }

                // Otherwise, merge with left?
                else if(prev != null)
                {
                    // We actually want to keep the left node, so add all of the keys in this node to the left node.
                    prev.keys.addAll(keys);
                    prev.values.addAll(values);
                    prev.next = next;
                    if(next != null)
                        next.prev = prev;

                    return 2;
                }

                // Otherwise, merge with right?
                else if(next != null)
                {
                    // Add all keys in right node to this node.
                    keys.addAll(next.keys);
                    values.addAll(next.values);
                    if(next.next != null)
                        next.next.prev = this;
                    next = next.next;

                    return 3;
                }

                // Otherwise, we're the root and it's okay to be less than leafOrder / 2.
                else
                {
                    return 1;
                }
            }
        }

        /**
         * Returns the guide index of to use when looking for the specified key.
         */
        private int findLeafIndex(K key)
        {
            return BPTreeIndex.this.findLeafIndex(this, key);
        }

        /**
         * Prints this Node and all keys to the specified StringWriter in XML format.
         */
        public void printXml(StringWriter out, int indent)
        {
            for(int i = 0; i < indent; i++)
                out.write("  ");
            out.write("<leaf>\n");

            // Print each entry.
            for(int i = 0; i < keys.size(); i++)
            {
                K key = keys.get(i);
                V value = values.get(i);

                // Print entry.
                for(int j = 0; j < indent + 1; j++)
                    out.write("  ");
                out.write("<entry key=\"" + key.toString() + "\" value=\"" + value.toString() + "\"/>\n");
            }

            for(int i = 0; i < indent; i++)
                out.write("  ");
            out.write("</leaf>\n");
        }
    }

    /**
     * A SortedMap which represents a sub-region of the key-space mapped by a BPTreeIndex.
     */
    private class SubMap extends AbstractMap<K,V>
    {
        private K low;
        private K high;

        private final EntrySet esInstance = new EntrySet();

        /**
         * Creates a new SubMap representing the sub-region between low (inclusive) and high (exclusive).
         */
        public SubMap(K low, K high)
        {
            this.low = low;
            this.high = high;
        }

        /**
         * Creates a new SubMap representing the entire BPTreeIndex.
         */
        public SubMap()
        {
            low = null;
            high = null;
        }

        /**
         * Returns whether the specified key is valid for this SubMap.
         */
        private boolean checkKey(K key)
        {
            return (low == null || key.compareTo(low) >= 0) && (high == null || key.compareTo(high) < 0);
        }

        /**
         * Returns whether this SubMap contains the specified key.
         */
        public boolean containsKey(K key)
        {
            return checkKey(key) && BPTreeIndex.this.containsKey(key);
        }

        /**
         * Returns the value associated with the specified key.
         */
        public V get(K key)
        {
            if(checkKey(key))
                return BPTreeIndex.this.get(key);
            else
                return null;
        }

        /**
         * Associates the specified value with the specified key in this SubMap.
         */
        public V put(K key, V value)
        {
            if(checkKey(key))
                return BPTreeIndex.this.put(key, value);
            else
                throw new IllegalArgumentException();
        }

        /**
         * Removes the specified key from this SubMap.
         */
        public V remove(K key)
        {
            if(checkKey(key))
                return BPTreeIndex.this.remove(key);
            else
                return null;
        }

        /**
         * Returns the first key in this SubMap.
         */
        public K firstKey()
        {
            for(K key : this.keySet())
                return key;

            throw new NoSuchElementException();
        }

        /**
         * Returns the last key in this SubMap.
         */
        public K lastKey()
        {
            K key = null;
            for(K k : this.keySet())
                key = k;

            if(key == null)
                throw new NoSuchElementException();

            return key;
        }

        /**
         * Returns a Set view of the Entries mapped in this SubMap.
         */
        public Set<Entry<K,V>> entrySet()
        {
            return esInstance;
        }

        /**
         * Returns a map representing a sub-range of the keys stored in this SubMap.
         */
        public SubMap subMap(K arg0, K arg1)
        {
            // Make sure specified bounds stay within the bounds of THIS SubMap.
            K newLow, newHigh;
            if(arg0 != null && arg0.compareTo(low) > 0)
                newLow = arg0;
            else
                newLow = low;
            if(arg1 != null && arg1.compareTo(high) < 0)
                newHigh = arg1;
            else
                newHigh = high;

            // Return a new SubMap.
            return BPTreeIndex.this.subMap(newLow, newHigh);
        }

        /**
         * Returns a map representing a sub-range of the keys stored in this SubMap.
         */
        public SubMap headMap(K arg0)
        {
            return subMap(firstKey(), arg0);
        }

        /**
         * Returns a map representing a sub-range of the keys stored in this SubMap.
         */
        public SubMap tailMap(K arg0)
        {
            return subMap(arg0, lastKey());
        }

        /**
         * A set of map entries backed by a SubMap.
         */
        private class EntrySet extends AbstractSet<Entry<K,V>>
        {
            /**
             * Removes all entries from this Set.
             */
            public void clear()
            {
                if(low == null && high == null)
                    BPTreeIndex.this.clear();
                else
                    super.clear();
            }

            /**
             * Returns whether this Set contains the specified entry.
             */
            public boolean contains(Object entry)
            {
                if(entry instanceof Entry)
                {
                    Entry<K,V> e = (Entry<K,V>)entry;
                    if(SubMap.this.containsKey(e.getKey()))
                    {
                        V value = SubMap.this.get(e.getKey());
                        return value == null ? e.getValue() == null : value.equals(e.getValue());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }

            /**
             * Returns an iterator which iterates over the elements in this Set.
             */
            public Iterator<Entry<K, V>> iterator()
            {
                return new EntrySetIterator();
            }

            /**
             * Removes the specified entry from this set.
             */
            public boolean remove(Object entry)
            {
                if(contains(entry))
                {
                    SubMap.this.remove(((Entry<K,V>)entry).getKey());
                    return true;
                }
                else
                {
                    return false;
                }
            }

            /**
             * Returns the number of elements in this set.
             */
            public int size()
            {
                if(low == null && high == null)
                    return BPTreeIndex.this.size();
                else
                {
                    int count = 0;
                    for(Entry<K,V> e : entrySet())
                    {
                        e = e == null ? null : null; // this line exists only to get rid of the "e is not used" warning.
                        count++;
                    }

                    return count;
                }
            }

            /**
             * Iterates through all of the entries in the set.
             */
            private class EntrySetIterator implements Iterator<Entry<K,V>>
            {
                private int modCount;

                private LeafNode curNode;
                private int curIndex = 0;
                private BPTEntry lastEntry = null;

                /**
                 * Creates a new BPTreeIterator.
                 */
                public EntrySetIterator()
                {
                    modCount = BPTreeIndex.this.modCount;
                    curNode = BPTreeIndex.this.firstLeaf;

                    // Keep getting next entry until we reach something >= low.
                    if(low != null)
                    {
                        // Find leaf node that contains the lowest allowable key.
                        Node cur = root;
                        while(cur instanceof BPTreeIndex.GuideNode)
                        {
                            GuideNode gn = (GuideNode)cur;
                            int index = findGuideIndex(gn, low);
                            cur = gn.children.get(index);
                        }

                        curNode = (LeafNode)cur;

                        // We may need to skip to the next node.
                        if(curNode.keys.get(curNode.keys.size() - 1).compareTo(low) < 0)
                            curNode = curNode.next;

                        // Find first key >= low.
                        if(curNode != null)
                        {
                            for(curIndex = 0; curIndex < curNode.keys.size() && curNode.keys.get(curIndex).compareTo(low) < 0; curIndex++);
                        }
                    }
                }

                /**
                 * Returs whether there are any entries left in the iteration.
                 */
                public boolean hasNext()
                {
                    return curNode != null && curIndex < curNode.keys.size() &&
                            (high == null || curNode.keys.get(curIndex).compareTo(high) < 0);
                }

                /**
                 * Returns the next entry in the iteration.
                 */
                public Entry<K,V> next()
                {
                    // Make sure tree has not been modified.
                    if(modCount != BPTreeIndex.this.modCount)
                        throw new ConcurrentModificationException();

                    // No more entries?
                    if(!hasNext())
                        throw new NoSuchElementException();

                    // Get entry.
                    lastEntry = new BPTEntry(curNode.keys.get(curIndex), BPTreeIndex.this);

                    // Increment index.
                    curIndex++;
                    if(curIndex >= curNode.keys.size())
                    {
                        curNode = curNode.next;
                        curIndex = 0;
                    }

                    // Return.
                    return lastEntry;
                }

                /**
                 * Removes the most recently retrieved element from the collection.
                 */
                public void remove()
                {
                    // Make sure tree has not been modified.
                    if(modCount != BPTreeIndex.this.modCount)
                        throw new ConcurrentModificationException();

                    // Make sure this isn't called during an invalid state.
                    if(lastEntry == null)
                        throw new IllegalStateException();

                    // Remove entry.
                    K curKey = curNode != null ? curNode.keys.get(curIndex) : null;
                    SubMap.this.remove(lastEntry.getKey());
                    if(curKey != null)
                        curIndex = curNode.findLeafIndex(curKey);

                    // Reset mod count.
                    modCount = BPTreeIndex.this.modCount;
                }
            }
        }
    }

    /**
     * An entry in a BPTreeIndex bound to a key and backed by the tree.
     */
    private class BPTEntry implements Entry<K,V>
    {
        private K key;
        private BPTreeIndex<K,V> tree;

        /**
         * Creates a new BPTEntry bound to the specified key and backed by the specified tree.
         */
        public BPTEntry(K key, BPTreeIndex<K,V> tree)
        {
            this.key = key;
            this.tree = tree;
        }

        /**
         * Returns the key to which this entry is bound.
         */
        public K getKey()
        {
            return key;
        }

        /**
         * Returns the value associated with this entry.
         */
        public V getValue()
        {
            return tree.get(key);
        }

        /**
         * Sets the value associated with this entry. The BPTreeIndex will be changed to reflect the new value.
         */
        public V setValue(V value)
        {
            return tree.put(key, value);
        }
    }
}

