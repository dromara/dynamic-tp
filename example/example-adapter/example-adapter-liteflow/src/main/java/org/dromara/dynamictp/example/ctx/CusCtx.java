package org.dromara.dynamictp.example.ctx;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * CusCtx related
 *
 * @author yanhom
 * @since 1.1.0
 */
@AllArgsConstructor(staticName = "of")
@Data
public class CusCtx {

    private Long id;

    private String name;
}
