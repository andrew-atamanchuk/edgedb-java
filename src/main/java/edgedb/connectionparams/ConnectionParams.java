package edgedb.connectionparams;

import lombok.*;

import java.net.URI;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionParams {

    private String dsn;

    @NonNull
    private String host="127.0.0.1";
//localhost:10701
    @NonNull
    private Integer port=10700;

    private String admin;

    @NonNull
    private String user="edgedb";

    @NonNull
    private String password;

    @NonNull
    private String database="edgedb";

    private int timeout;

//    public ConnectionParams(String dsn) {
//        URI uri = URI.create(dsn);
//        user = uri.getUserInfo();
//        host = uri.getHost();
//        port = uri.getPort();
//    }
}
