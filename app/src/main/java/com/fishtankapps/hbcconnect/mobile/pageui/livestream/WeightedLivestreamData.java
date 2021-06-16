package com.fishtankapps.hbcconnect.mobile.pageui.livestream;

import com.fishtankapps.hbcconnect.mobile.storage.LivestreamData;

public class WeightedLivestreamData implements Comparable<WeightedLivestreamData> {

    private final LivestreamData livestreamData;
    private final int weight;

    public WeightedLivestreamData(LivestreamData livestreamData, int weight) {
        this.livestreamData = livestreamData;
        this.weight = weight;
    }

    public LivestreamData getLivestreamData() {
        return livestreamData;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(WeightedLivestreamData o) {
        int diff = (getWeight() - o.getWeight());

        if(diff == 0)
            return getLivestreamData().compareTo(o.getLivestreamData());

        return diff;
    }
}
