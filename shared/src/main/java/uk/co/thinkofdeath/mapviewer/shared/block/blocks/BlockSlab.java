/*
 * Copyright 2014 Matthew Collins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.thinkofdeath.mapviewer.shared.block.blocks;

import uk.co.thinkofdeath.mapviewer.shared.Face;
import uk.co.thinkofdeath.mapviewer.shared.IMapViewer;
import uk.co.thinkofdeath.mapviewer.shared.Texture;
import uk.co.thinkofdeath.mapviewer.shared.block.Block;
import uk.co.thinkofdeath.mapviewer.shared.block.BlockFactory;
import uk.co.thinkofdeath.mapviewer.shared.block.StateMap;
import uk.co.thinkofdeath.mapviewer.shared.block.states.BooleanState;
import uk.co.thinkofdeath.mapviewer.shared.block.states.EnumState;
import uk.co.thinkofdeath.mapviewer.shared.model.Model;
import uk.co.thinkofdeath.mapviewer.shared.model.ModelFace;

public class BlockSlab extends BlockFactory {

    public static final String VARIANT = "variant";
    public static final String TOP = "top";

    private final Texture[] textures;

    public BlockSlab(IMapViewer iMapViewer, Class<? extends Enum> clazz) {
        super(iMapViewer);
        addState(VARIANT, new EnumState(clazz));
        addState(TOP, new BooleanState());

        textures = new Texture[clazz.getEnumConstants().length * 6];

        int i = 0;
        for (Enum e : clazz.getEnumConstants()) {
            SlabType type = (SlabType) e;
            for (Face face : Face.values()) {
                textures[i++] = iMapViewer.getTexture(type.texture(face));
            }
        }
    }

    @Override
    protected Block createBlock(StateMap states) {
        return new BlockImpl(states);
    }

    private static interface SlabType {
        int ordinal();

        String texture(Face face);
    }

    public static enum StoneSlab implements SlabType {
        STONE("") {
            @Override
            public String texture(Face face) {
                return face == Face.TOP || face == Face.BOTTOM ?
                        "stone_slab_top" :
                        "stone_slab_side";
            }
        },
        SANDSTONE("") {
            @Override
            public String texture(Face face) {
                return face == Face.TOP || face == Face.BOTTOM ?
                        "sandstone_top" :
                        "sandstone_normal";
            }
        },
        WOODEN("planks_oak"),
        COBBLESTONE("cobblestone"),
        BRICK("brick"),
        STONE_BRICK("stonebrick"),
        NETHER_BRICK("nether_brick"),
        QUARTZ("") {
            @Override
            public String texture(Face face) {
                return face == Face.TOP || face == Face.BOTTOM ?
                        "quartz_block_top" :
                        "quartz_block_side";
            }
        },;

        private final String texture;

        StoneSlab(String texture) {
            this.texture = texture;
        }

        @Override
        public String texture(Face face) {
            return texture;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public static enum WoodenSlab implements SlabType {
        OAK("planks_oak"),
        SPRUCE("planks_spruce"),
        BIRCH("planks_birch"),
        JUNGLE("planks_jungle"),
        ACACIA("planks_acacia"),
        DARK_OAK("planks_big_oak");

        private final String texture;

        WoodenSlab(String texture) {
            this.texture = texture;
        }

        @Override
        public String texture(Face face) {
            return texture;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private class BlockImpl extends Block {

        BlockImpl(StateMap state) {
            super(BlockSlab.this, state);
        }

        @Override
        public int getLegacyData() {
            int val = this.<SlabType>getState(VARIANT).ordinal();
            if (this.<Boolean>getState(TOP)) {
                val |= 0x8;
            }
            return val;
        }

        @Override
        public Model getModel() {
            if (model == null) {
                model = new Model();

                boolean top = getState(TOP);
                SlabType type = getState(VARIANT);

                int i = type.ordinal() * 6;

                model.addFace(new ModelFace(Face.TOP,
                        textures[i + Face.TOP.ordinal()], 0, 0, 16, 16, top ? 16 : 8, top));
                model.addFace(new ModelFace(Face.BOTTOM,
                        textures[i + Face.BOTTOM.ordinal()], 0, 0, 16, 16, top ? 8 : 0, !top));
                model.addFace(new ModelFace(Face.FRONT,
                        textures[i + Face.FRONT.ordinal()], 0, top ? 8 : 0, 16, 8, 16, true));
                model.addFace(new ModelFace(Face.BACK,
                        textures[i + Face.BACK.ordinal()], 0, top ? 8 : 0, 16, 8, 0, true));
                model.addFace(new ModelFace(Face.LEFT,
                        textures[i + Face.LEFT.ordinal()], 0, top ? 8 : 0, 16, 8, 16, true));
                model.addFace(new ModelFace(Face.RIGHT,
                        textures[i + Face.RIGHT.ordinal()], 0, top ? 8 : 0, 16, 8, 0, true));
            }
            return model;
        }
    }
}
