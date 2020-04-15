package top.mgy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

/**
 * 封装对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Content {
    private String title;
    private String img;
    private String price;
}
