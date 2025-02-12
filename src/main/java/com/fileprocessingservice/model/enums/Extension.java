package com.fileprocessingservice.model.enums;

import com.fileprocessingservice.model.dto.UserRequest;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Log4j2
public enum Extension {
    CSV(".csv") {
        @Override
        public List<UserRequest> parseUserFile(byte[] file) throws IOException {
            List<UserRequest> users = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        String username = data[0];
                        String email = data[1];
                        String firstName = data[2];
                        String lastName = data[3];
                        users.add(new UserRequest(
                                username,
                                email,
                                firstName,
                                lastName));
                    } else {
                        log.error("parseUserFile :: wrong number of fields in the line");
                    }
                }
            }
            return users;
        }
    };

    private static final HashMap<String, Extension> EXTENSIONS = new HashMap<>();
    private final String extension;

    static {
        for (Extension ext : values()) {
            EXTENSIONS.put(ext.getExtension(), ext);
        }
    }

    Extension(String extension) {
        this.extension = extension;
    }

    public abstract List<UserRequest> parseUserFile(byte[] file) throws IOException;

    public static Extension valueOfExtension(String extension) {
        if (extension == null) {
            return null;
        }

        return EXTENSIONS.get(extension);
    }
}
