package ua.in.denoming.horstmann.example09;

abstract class Worker {
    public abstract void body() throws Exception;

    Thread toThread() {
        return new Thread(() -> {
            synchronized(this){
                try {
                    body();
                } catch (Throwable e) {
                    //noinspection RedundantTypeArguments
                    Worker.<RuntimeException>throwAs(e);
                } finally {
                    notify();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwAs(Throwable e) throws T {
        throw (T) e;
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Before");
        Thread t = Main.doWork();
        t.start();

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(t) {
            try {
                t.wait();
                System.out.println("After");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static private Thread doWork() {
        return new Worker() {
            @Override
            public void body() throws Exception {
                Thread.sleep(1000);
            }
        }.toThread();
    }
}
