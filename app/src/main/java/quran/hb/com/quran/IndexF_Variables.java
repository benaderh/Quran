package quran.hb.com.quran;

/**
 * Created by Juned on 2/21/2017.
 */

public class IndexF_Variables {
        //String name = null;
        //String number = null;
        String sora = null;
        String ayat = null;
        String ayai = null;
        String page = null;
        String line = null;
        String point = null;

        public IndexF_Variables(String Ssora, String Sayat, String Sayai, String Spage , String Sline, String Spoint) {

                super();

                this.sora = Ssora;
                this.ayat = Sayat;
                this.ayai = Sayai;
                this.page = Spage;
                this.line = Sline;
                this.point = Spoint;
        }



        public String getSora() {
                return sora;
        }
        public void setSora(String sora2) {
                this.sora = sora2;
        }



        public String getAyat() {
                return ayat;
        }
        public void setAyat(String ayat2) {
                this.ayat = ayat2;
        }



        public String getAyai() {
                return ayai;
        }
        public void setAyai(String ayai2) {
                this.ayai = ayai2;
        }



        public String getPage() {
                return page;
        }
        public void setPage(String page2) {
                this.page = page2;
        }



        public String getLine() {
                return line;
        }
        public void setLine(String line2) {
                this.line = line2;
        }



        public String getPoint() {
                return point;
        }
        public void setPoint(String point2) {
                this.point = point2;
        }


        @Override
        public String toString() {

                //return  name + " " + number ;
                return  ayat ;

        }

}
