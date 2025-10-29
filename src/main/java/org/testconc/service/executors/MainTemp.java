package org.testconc.service.executors;

import java.util.*;

public class MainTemp {

    public static void main(String[] args) {
        /*Solution s = new Solution();
//        long l = s.minCost(new int[]{84, 80, 43, 8, 80, 88, 43, 14, 100, 88}, new int[]{32, 32, 42, 68, 68, 100, 42, 84, 14, 8});
//        long l = s.minCost(new int[]{4,2,2,2}, new int[]{1,4,1,2});
        long l = s.minCost(new int[]{4,4,4,4,3}, new int[]{5,5,5,5,3});
        System.out.println(l);*/

        @SuppressWarnings("removal")
        Integer a = new Integer(1);
        @SuppressWarnings("removal")
        Integer b = new Integer(1);
        System.out.println(a == b);

    }

    static class Solution {
        private int[] aux;
        Map<Integer, Integer> b1map;
        Map<Integer, Integer> b2map;

        public long minCost(int[] basket1, int[] basket2) {
            int commonIndex = 0;

            b1map = new HashMap<>();
            b2map = new HashMap<>();
            int min = Integer.MAX_VALUE;

            for (int i = 0; i < basket1.length; i++) {
                if (basket1[i] <= basket2[i] && basket1[i] < min){
                    min = basket1[i];
                } else if(basket1[i] > basket2[i] && basket2[i] < min) {
                    min = basket2[i];
                }
                int value1 = b1map.getOrDefault(basket1[i], 0);
                b1map.put(basket1[i], ++value1);
                int value2 = b2map.getOrDefault(basket2[i], 0);
                b2map.put(basket2[i], ++value2);
            }
            int swapsCost = 0;
            for(Map.Entry entry : b1map.entrySet()) {
                Object key = entry.getKey();
                Integer value1 = (Integer)entry.getValue();
                Integer value2 = b2map.remove(key);
                if(value2 != null) {
                    if(((value1 + value2) & 1) == 1){
                        return -1;
                    }
                    int diff = (value1 - value2)/2;
                    if((int)key == min) {
                        diff /= 2;
                    }
                    swapsCost = swapsCost + (diff < 0 ? -diff : diff)*min;
                } else {
                    if((value1 & 1) == 1){
                        return -1;
                    }
                    int diff = value1 / 2;
                    if((int)key == min) {
                        diff /= 2;
                    }
                    swapsCost = swapsCost + diff*min;
                }
            }
            for(Map.Entry entry : b2map.entrySet()) {
                Object key = entry.getKey();
                Integer value2 = (Integer)entry.getValue();
                Integer value1 = b1map.remove(key);
                if(value1 != null) {
                    if(((value1 + value2) & 1) == 1){
                        return -1;
                    }
                    int diff = (value1 - value2)/2;
                    if((int)key == min) {
                        diff /= 2;
                    }
                    swapsCost = swapsCost + (diff < 0 ? -diff : diff)*min;
                } else {
                    if((value2 & 1) == 1){
                        return -1;
                    }
                    int diff = value2 / 2;
                    if((int)key == min) {
                        diff /= 2;
                    }
                    swapsCost = swapsCost + diff*min;
                }
            }

            return swapsCost;
        }

        public void mergeSort(int[] a) {
            aux = new int[a.length];
            doSort(a);
        }

        public void doSort(int[] a) {
            for (int i = 1; i < a.length; i *= 2) {
                for (int k = 0; k < a.length - i; k += 2 * i) {
                    int l = k + i + i - 1;
                    int r = a.length - 1;
                    merge(a, k, (k + i), l < r ? l : r);
                }

            }
        }

        public void merge(int[] a, int lo, int mid, int hi) {
            int i = lo; //left
            int t = lo;
            int j = mid; //right

            for (int k = lo; k <= hi; k++) aux[k] = a[k];

            while (t <= hi) {
                if (j > hi) a[t++] = aux[i++];
                else if (i >= mid) a[t++] = aux[j++];
                else if (aux[i] <= aux[j]) a[t++] = aux[i++];
                else a[t++] = aux[j++];
            }
        }
    }
}
