package com.github.cactuspuppy.gameapi.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<A, B> {
    private A first;
    private B second;
}
