package cs455.overlay.wireformats;

public class EnumToByte {
    public static byte[] toByteArray(Enum<?> e) {
        return e.name().getBytes();
    }
}
