package com.shaverz.cream.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyTransactionGenerator {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Transaction> ITEMS = new ArrayList<Transaction>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Transaction> ITEM_MAP = new HashMap<String, Transaction>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyTransaction(i));
        }
    }

    private static void addItem(Transaction item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Transaction createDummyTransaction(int position) {
        try{
            return new Transaction(
                    Integer.toString(position),
                    position * 100,
                    Transaction.DEPOSIT,
                    "Bank",
                    new Date());
        } catch (Exception e) {
            return null;
        }
    }

}
