import java.util.HashSet;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.split.SplitWordTool2;



public class TestParticiple {

    /**
     * @param args
     */
    public static void main(String[] args) {
        HashSet<Cell> set = SplitWordTool2.splitWordPort("捕鱼大人");
        for (Cell c : set) {
            System.out.println("=" + c.getName());
        }
    }

}
