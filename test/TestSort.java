import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class TestSort {

    private static class Tuple implements Comparable<Tuple> {
        public Tuple(String word, int offsetBegin, int offsetEnd) {
            this.word = word;
            this.offsetBegin = offsetBegin;
            this.offsetEnd = offsetEnd;
        }

        public final String word;
        public final int offsetBegin;
        public final int offsetEnd;
        public int position;

        /**
         * 起始位置靠前的，先出现，起始位置相同的，长串先出现
         */
        @Override
        public int compareTo(Tuple o) {
            if (this.offsetBegin == o.offsetBegin) {
                return o.offsetBegin - this.offsetEnd;
            } else {
                return this.offsetBegin - o.offsetBegin;
            }
        }
    }

    public static void main(String[] args) {
        List<Tuple> list = new ArrayList<Tuple>();
        list.add(new Tuple("", 0, 1));
        list.add(new Tuple("", 0, 2));
        list.add(new Tuple("", 5, 8));
        list.add(new Tuple("", 4, 6));
        Collections.sort(list);
        for (Tuple t : list) {
            System.out.println(ToStringBuilder.reflectionToString(t));
        }
    }
}
