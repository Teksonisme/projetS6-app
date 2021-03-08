package com.example.orientation_app;

import java.util.Comparator;

class InterestBearingComparator implements Comparator<Interest> {

        public int compare(Interest interest1, Interest interest2) {
            return (int) (interest1.getBearingFromUser() - interest2.getBearingFromUser());
        }
    }