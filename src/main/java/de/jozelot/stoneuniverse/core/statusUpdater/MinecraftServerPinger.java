package de.jozelot.stoneuniverse.core.statusUpdater;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MinecraftServerPinger {

    private final String host;
    private final int port;
    private final int timeout;

    private boolean online = false;
    private int onlinePlayers = 0;
    private int maxPlayers = 0;
    private String motd = "";
    private boolean maintenance = false;

    public MinecraftServerPinger(String host, int port, int timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeout = timeoutMs;
    }

    public void fetchStatus() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(this.host, this.port), this.timeout);
            socket.setSoTimeout(this.timeout);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            ByteArrayOutputStream handshakeBuffer = new ByteArrayOutputStream();
            DataOutputStream handshakeOut = new DataOutputStream(handshakeBuffer);

            writeVarInt(handshakeOut, 0x00);
            writeVarInt(handshakeOut, 765);
            writeString(handshakeOut, this.host);
            handshakeOut.writeShort(this.port);
            writeVarInt(handshakeOut, 1);

            writeVarInt(out, handshakeBuffer.size());
            out.write(handshakeBuffer.toByteArray());

            writeVarInt(out, 1);
            out.writeByte(0x00);

            readVarInt(in);
            int id = readVarInt(in); // Paket ID (0x00 für Response)

            if (id == 0x00) {
                String jsonString = readString(in);
                this.online = true;

                this.onlinePlayers = parseJsonInt(jsonString, "\"online\":");
                this.maxPlayers = parseJsonInt(jsonString, "\"max\":");

                if (jsonString.contains("\"text\":\"")) {
                    this.motd = jsonString.split("\"text\":\"")[1].split("\"")[0];
                }

                if (jsonString.toLowerCase().contains("maintenance") ||
                        jsonString.toLowerCase().contains("wartung") ||
                        this.motd.toLowerCase().contains("wartung")) {
                    this.maintenance = true;
                } else {
                    this.maintenance = false;
                }
            }
        } catch (IOException e) {
            this.online = false;
            this.onlinePlayers = 0;
            this.maxPlayers = 0;
            this.motd = "";
            this.maintenance = false;
        }
    }

    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt zu groß");
            if ((k & 0x80) != 0x80) break;
        }
        return i;
    }

    private void writeString(DataOutputStream out, String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private String readString(DataInputStream in) throws IOException {
        readVarInt(in);
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private int parseJsonInt(String json, String key) {
        if (!json.contains(key)) return 0;
        String sub = json.split(key)[1].trim();
        StringBuilder num = new StringBuilder();
        for (char c : sub.toCharArray()) {
            if (Character.isDigit(c)) {
                num.append(c);
            } else if (num.length() > 0) {
                break;
            }
        }
        return num.length() > 0 ? Integer.parseInt(num.toString()) : 0;
    }

    public boolean isOnline() { return online; }
    public int getOnlinePlayers() { return onlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getMotd() { return motd; }
    public boolean isMaintenance() { return maintenance; }
}