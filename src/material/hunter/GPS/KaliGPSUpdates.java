package material.hunter.GPS;

public interface KaliGPSUpdates {

    interface Receiver {
        void onPositionUpdate(String nmeaSentences);

        void onFirstPositionUpdate();
    }

    interface Provider {
        void onLocationUpdatesRequested(Receiver receiver);

        boolean onReceiverReattach(Receiver receiver);

        void onStopRequested();
    }
}
