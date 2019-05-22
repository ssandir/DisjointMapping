import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class Seminar2 {
    private static int n;

    private static class Point implements Comparator<Point> {
        int i;
        double x;
        double y;
        boolean inhull;
        boolean inuse;

        static Point frst;
        static Point last;
        static Point bp;
        static Point ytop;

        Point next;
        Point prev;

        Point yp;
        Point yn;

        Point hp;
        ArrayList<Point> hn;

        Point(){}

        Point(int i_in,double x_in, double y_in) {
            i = i_in;
            x = x_in;
            y = y_in;
            next = null;
            inhull=false;
            hp=null;
            hn=new ArrayList<>();
            inuse=true;
            yp=null;
            yn=null;
        }

        public int compare(Point a, Point b){
            if(a.x<b.x) return -1;
            return 1;
        }

        public void connect(Point p){
            p.hp = this;
            hn.add(p);
            p.inhull = true;
            if(x<0&&p.x>0) bp = this;
        }

        public void remove(){
            inuse = false;
            if(frst==this) {
                frst = next;
                frst.hp=null;
            } else prev.next = next;
            if(last==this) last = prev;
            else next.prev = prev;
            if(ytop==this) {
                ytop = yn;
            } else yp.yn = yn;
            if(yn!=null) {
                yn.yp = yp;
            }
        }

        public static void printHull(){
            Point it = frst;
            while(it!=null){
                System.out.print(it.i+" ");
                if(it.hn.size()==0) break;
                while(!it.hn.get(it.hn.size()-1).inuse){
                    it.hn.remove(it.hn.size()-1);
                    if(it.hn.size()==0) break;
                }
                if(it.hn.size()==0) break;
                it=it.hn.get(it.hn.size()-1);
                if(it==frst) break;
            }
            System.out.println();
        }
    }

    private static class Y implements Comparator<Point> {
        public int compare(Point a, Point b){
            if(a.y>b.y) return -1;
            return 1;
        }
    }

    private static boolean leftTurn(Point p1,Point p2, Point p3){
        if(p1==null) return false;
        Double det = (p1.x*p2.y - p1.y*p2.x)+(p1.y*p3.x - p1.x*p3.y)+(p2.x*p3.y - p2.y*p3.x);
        return det>0;
    }

    private static void makeuHull(Point frst){
        //init
        Point it = frst;
        Point np;
        it.inhull = true;

        while((np = it.next)!=null){
            while(leftTurn(it.hp,it,np)){
                it.inhull=false;
                it.hn.add(np);
                it = it.hp;
            }
            it.connect(np);
            it=np;
        }
    }

    private static void remakeHull(Point it){
        Point np;
        boolean go = true;
        it.inhull = true;
        while(it.hn.size()>0&&!it.hn.get(it.hn.size()-1).inuse) {
            it.hn.remove(it.hn.size() - 1);
        }
        np = (it.hn.size()==0) ? it.next : it.hn.get(it.hn.size()-1);

        while(np!=null && go){
            go = !np.inhull;
            while(leftTurn(it.hp,it,np)){
                it.inhull=false;
                it.hn.add(np);
                it = it.hp;
            }
            it.connect(np);
            it=np;
            while(it.hn.size()>0&&!it.hn.get(it.hn.size()-1).inuse) {
                it.hn.remove(it.hn.size() - 1);
            }
            np = (it.hn.size()==0) ? it.next : it.hn.get(it.hn.size()-1);
        }
    }

    private static void solve(ArrayList<Point> points)  throws IOException{
            long ssum;
            long hsum;
            long wsum=0;
            long stimes = System.nanoTime();
            long stimew;
        OutputStream out = new BufferedOutputStream ( System.out );

        Collections.sort(points, new Y());
        for(int i=0;i<points.size();++i){
            if(i<points.size()-1) points.get(i).yn=points.get(i+1);
            if(i>0) points.get(i).yp=points.get(i-1);
        }
        Point.ytop=points.get(0);

        Collections.sort(points, new Point());
        for(int i=0;i<points.size();++i){
            if(i<points.size()-1) points.get(i).next=points.get(i+1);
            if(i>0) points.get(i).prev=points.get(i-1);
        }

        Point.frst = points.get(0);
        Point.last = points.get(points.size()-1);

        ssum = (System.nanoTime() - stimes) / 1000000;
        long stimeh = System.nanoTime();


        makeuHull(points.get(0));
        hsum = (System.nanoTime() - stimeh) / 1000000;
        int pc = n;
        Point rms;
        while(pc>2){
                stimew = System.nanoTime();

            out.write((Point.bp.i + " - " + Point.bp.hn.get(Point.bp.hn.size()-1).i + "\n").getBytes());

                wsum+=System.nanoTime()-stimew;

            rms = Point.bp.hp;
            Point.bp.hn.get(Point.bp.hn.size()-1).remove();
            Point.bp.remove();
            pc-=2;

            while(pc>2&&Point.ytop.x>0 != Point.ytop.yn.x>0){
                if(Point.ytop == rms) rms = Point.ytop.hp;
                if(Point.ytop.yn == rms) rms = Point.ytop.yn.hp;
                out.write((Point.ytop.i + " - " + Point.ytop.yn.i + "\n").getBytes());
                Point.ytop.remove();
                Point.ytop.remove();
                pc-=2;
            }
            if(rms==null) rms = Point.frst;
            remakeHull(rms);
        }
        stimew = System.nanoTime();
        out.write((Point.frst.i + " - " + Point.last.i).getBytes());
        out.flush();
        wsum+=System.nanoTime()-stimew;
        System.out.println();
        System.out.println("s "+ssum+"  h "+hsum+"  w "+wsum/1000000);
    }

    public static void main(String[] args) throws IOException {
        long stime = System.nanoTime();

        String fileName;
        //fileName = args[0];
        fileName = "InputFile2.txt";

        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        ArrayList<Point> data = new ArrayList<>();
        String in = br.readLine();
        n=Integer.parseInt(in);
        for(int i=0;i<n;++i) {
            in = br.readLine();
            String[] inp = in.split(",");
            data.add(new Point(Integer.parseInt(inp[0]), Double.parseDouble(inp[1]), Double.parseDouble(inp[2])));
        }
        solve(data);

        long etime = System.nanoTime();
        System.out.print(" " + (etime - stime) / 1000000 + " ");
    }
}
