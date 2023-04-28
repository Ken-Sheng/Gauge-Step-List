package org.ken.sheng.gaugesteplist;

import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

public interface ISource {

    /**
     * 获取显示的字符串
     *
     * @return str
     */
    @NotNull
    String getFragment();

    @NotNull
    default SimpleTextAttributes getTextAttributes() {
        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }
}
