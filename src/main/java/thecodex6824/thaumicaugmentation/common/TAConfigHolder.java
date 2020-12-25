/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.common;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.ImmutableSet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAConfig.TileWardMode;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionBoolean;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDouble;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDoubleList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionEnum;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionFloat;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionInt;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntSet;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionLong;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionStringList;
import thecodex6824.thaumicaugmentation.api.config.IEnumSerializer;
import thecodex6824.thaumicaugmentation.api.config.TAConfigManager;
import thecodex6824.thaumicaugmentation.common.network.PacketConfigSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

/**
 * Holds configuration variables for Thaumic Augmentation.
 * @author TheCodex6824
 */
@Config(modid = ThaumicAugmentationAPI.MODID, category = "")
@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class TAConfigHolder {

    private TAConfigHolder() {}
    
    // TODO localize all the strings here
    
    @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.general")
    public static GeneralOptions general = new GeneralOptions();
    
    @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.gameplay")
    public static GameplayOptions gameplay = new GameplayOptions();
    
    @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.world")
    public static WorldOptions world = new WorldOptions();
    
    @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.client")
    public static ClientOptions client = new ClientOptions();
    
    public static class GeneralOptions {
        
        @Name("DisableCoremod")
        @Comment({
            "Completely disables the Thaumic Augmentation coremod.",
            "It will still appear in the list of loaded coremods, but won't do anything.",
            "The coremod is a neccessary evil to get warded blocks to behave properly with other mods,",
            "as well as work around other vanilla and Thaumcraft issues."
        })
        @RequiresMcRestart
        public boolean disableCoremod = false;
        
        @Name("DisabledTransformers")
        @Comment({
            "An optional list of coremod class transformers to disable.",
            "For advanced users / modpack makers that encounter issues with only a subset of the coremod.",
            "This takes the fully qualified class name of THE TRANSFORMER CLASS ITSELF, and only does anything if the coremod itself is enabled.",
            "An example would be adding \"thecodex6824.thaumicaugmentation.core.transformer.TransformerBipedRotationCustomTCArmor\" without quotes",
            "to disable the modifications to Thaumcraft's ModelCustomArmor.",
            "If you do have to add exclusions here, reporting the issues as well would be greatly appreciated."
        })
        @RequiresMcRestart
        public String[] disabledTransformers = new String[0];
        
    }
    
    public static class GameplayOptions {
        
        @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.ward")
        public WardOptions ward = new WardOptions();
        
        @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.impetus")
        public Impetus impetus = new Impetus();
        
        @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.augment")
        public Augment augment = new Augment();
        
        public static class WardOptions {
            
            @Name("AllowSingleplayerWardOverride")
            @Comment({
                "Allows you to always be able to interact with or destroy any warded block/tile while in singleplayer.",
                "For multiplayer see AllowOPWardOverride."
            })
            public boolean singlePlayerWardOverride = false;
            
            @Name("AllowOPWardOverride")
            @Comment({
                "Allow server operators to always be able to interact with or destroy any warded block/tile.",
                "For singleplayer see AllowSingleplayerWardOverride."
            })
            public boolean opWardOverride = false;
            
            @Name("DisableWardFocus")
            @Comment({
                "Disables the ward focus. This will remove the research entry, disable existing wards, and make existing foci do nothing.",
                "This is a server-side setting, although the ward research may not sync properly if the value is not the same on both sides."
            })
            @RequiresMcRestart
            public boolean disableWardFocus = false;
            
            @Name("WardTileMode")
            @Comment({
                "Optionally allows tile entities to be warded in addition to normal blocks.",
                "While \"all\" and \"none\" should be self explanatory, \"notick\" will",
                "only allow tiles that do not tick (aka do not implement ITickable).",
                "Allowing all tiles may be very overpowered - use at your own risk!"
            })
            public TileWardMode tileWardMode = TileWardMode.NOTICK;
            
        }
        
        public static class Impetus {
            
            @LangKey(ThaumicAugmentationAPI.MODID + ".text.config.impulse_cannon")
            public ImpulseCannon cannon = new ImpulseCannon();
            
            @Name("TerraformerCost")
            @Comment({
                "The amount of Impetus the Arcane Terraformer consumes per block terraformed."
            })
            public int terraformerCost = 5;
            
            @Name("ShieldFocusCost")
            @Comment({
                "The amount of Impetus the Void Shield focus effect consumes to create the shield.",
                "Note that a proportion of this amount will be consumed to heal a damaged shield."
            })
            public int shieldFocusCost = 10;
            
            public static class ImpulseCannon {
                
                @Name("BeamDamage")
                @Comment({
                    "The amount of damage that the Impulse Cannon's beam attack does.",
                    "The beam attack is the default attack with no augments.",
                    "The beam does not reset the damage cooldowns of entities damaged by it,",
                    "so while this damage can theoretically be seen per tick, in practice this is",
                    "extremely unlikely and would take a large crowd and good aim to achieve."
                })
                public float beamDamage = 7.0F;
                
                @Name("BeamCost")
                @Comment({
                    "The amount of Impetus used by the Impulse Cannon's beam attack per tick.",
                    "This cost is paid even if nothing is being hit by the beam."
                })
                public int beamCost = 1;
                
                @Name("BeamRange")
                @Comment({
                    "The range in meters of the Impulse Cannon's beam attack."
                })
                public double beamRange = 32.0;
                
                @Name("RailgunDamage")
                @Comment({
                    "The amount of damage that the Impulse Cannon's railgun attack does.",
                    "Note that the beam can pierce through multiple entities, but not blocks."
                })
                public float railgunDamage = 30.0F;
                
                @Name("RailgunCost")
                @Comment({
                    "The amount of Impetus used by the Impulse Cannon's railgun attack per shot.",
                    "This cost is paid even if nothing is being hit by the shot."
                })
                public int railgunCost = 5;
                
                @Name("RailgunCooldown")
                @Comment({
                    "The cooldown in ticks between shots of the Impulse Cannon in railgun mode.",
                    "Note that this will lock the player out of all Impulse Cannons in their inventory for",
                    "this duration."
                })
                public int railgunCooldown = 100;
                
                @Name("RailgunRange")
                @Comment({
                    "The range in meters of the Impulse Cannon's railgun attack."
                })
                public double railgunRange = 64.0;
                
                @Name("BurstDamage")
                @Comment({
                    "The amount of damage that the Impulse Cannon's burst attack does per shot.",
                    "Note that the damage cooldown of an entity hit by the first 2 rounds of the burst is reset",
                    "to allow the other rounds to do damage.",
                    "Since there are three shots fired by the burst, the effective damage is three times this value."
                })
                public float burstDamage = 8.0F;
                
                @Name("BurstCost")
                @Comment({
                    "The amount of Impetus used by the Impulse Cannon's burst attack per burst.",
                    "This cost is paid even if nothing is being hit by the shot."
                })
                public int burstCost = 4;
                
                @Name("BurstCooldown")
                @Comment({
                    "The cooldown in ticks between shots of the Impulse Cannon in burst mode.",
                    "Note that this will lock the player out of all Impulse Cannons in their inventory for",
                    "this duration."
                })
                public int burstCooldown = 26;
                
                @Name("BurstRange")
                @Comment({
                    "The range in meters of the Impulse Cannon's burst attack."
                })
                public double burstRange = 24.0;
                
            }
        }
        
        public static class Augment {
            
            @Name("ExperienceModifierCap")
            @Comment({
                "The maximum benefit the Experience Modifier can provide."
            })
            public double experienceModifierCap = 2.0;
            
            @Name("ExperienceModifierBase")
            @Comment({
                "The minimum benefit the Experience Modifier can provide.",
                "1.0 is no benefit, and lower values will reduce effectiveness."
            })
            public double experienceModifierBase = 1.0;
            
            @Name("ExperienceModifierScaleFactor")
            @Comment({
                "The number the player's level is multiplied by, before being clamped.",
                "In other words, this controls how many levels it takes to reach the maximum value."
            })
            public double experienceModifierScale = 0.04;
            
            @Name("ElementalModifierPostiveFactor")
            @Comment({
                "The number to be multiplied into the total effectiveness of the Elemental Modifier for a postive aspect."
            })
            public double elementalModifierPositiveFactor = 1.75;
            
            @Name("ElementalModifierNegativeFactor")
            @Comment({
                "The number to be multiplied into the total effectiveness of the Elemental Modifier for a postive aspect."
            })
            public double elementalModifierNegativeFactor = 0.75;
            
            @Name("DimensionalModifierOverworldPostiveFactor")
            @Comment({
                "The strength of the dimensional modifier in the correct dimension."
            })
            public double dimensionalModifierOverworldPositiveFactor = 1.25;
            
            @Name("DimensionalModifierOverworldNegativeFactor")
            @Comment({
                "The strength of the dimensional modifier in the incorrect dimension."
            })
            public double dimensionalModifierOverworldNegativeFactor = 1.0;
            
            @Name("DimensionalModifierOverworldDims")
            @Comment({
                "The list of dimensions that should count as being in the Overworld for the overworld modifier."
            })
            public Integer[] dimensionalModifierOverworldDims = new Integer[] {
                    0
            };
            
            @Name("DimensionalModifierNetherPostiveFactor")
            @Comment({
                "The strength of the dimensional modifier in the correct dimension."
            })
            public double dimensionalModifierNetherPositiveFactor = 1.5;
            
            @Name("DimensionalModifierNetherNegativeFactor")
            @Comment({
                "The strength of the dimensional modifier in the incorrect dimension."
            })
            public double dimensionalModifierNetherNegativeFactor = 1.0;
            
            @Name("DimensionalModifierNetherDims")
            @Comment({
                "The list of dimensions that should count as being in the Nether for the overworld modifier."
            })
            public Integer[] dimensionalModifierNetherDims = new Integer[] {
                    -1
            };
            
            @Name("DimensionalModifierEndPostiveFactor")
            @Comment({
                "The strength of the dimensional modifier in the correct dimension."
            })
            public double dimensionalModifierEndPositiveFactor = 1.5;
            
            @Name("DimensionalModifierEndNegativeFactor")
            @Comment({
                "The strength of the dimensional modifier in the incorrect dimension."
            })
            public double dimensionalModifierEndNegativeFactor = 1.0;
            
            @Name("DimensionalModifierEndDims")
            @Comment({
                "The list of dimensions that should count as being in the End for the overworld modifier."
            })
            public Integer[] dimensionalModifierEndDims = new Integer[] {
                    1
            };
            
            @Name("DimensionalModifierEmptinessPostiveFactor")
            @Comment({
                "The strength of the dimensional modifier in the correct dimension."
            })
            public double dimensionalModifierEmptinessPositiveFactor = 1.5;
            
            @Name("DimensionalModifierEmptinessNegativeFactor")
            @Comment({
                "The strength of the dimensional modifier in the incorrect dimension."
            })
            public double dimensionalModifierEmptinessNegativeFactor = 1.0;
            
            @Name("DimensionalModifierEmptinessDims")
            @Comment({
                "The list of dimensions that should count as being in the Emptiness for the overworld modifier.",
                "If the game automatically changes the Emptiness dim ID, it will be updated here,",
                "otherwise, you will need to specify it manually if changed."
            })
            public Integer[] dimensionalModifierEmptinessDims = new Integer[] {
                    14676
            };
            
            @Name("FrenzyModifierCooldown")
            @Comment({
                "The amount of ticks without hurting things until the frenzy boost wears off."
            })
            public int frenzyModifierCooldown = 100;
            
            @Name("FrenzyModifierScaleFactor")
            @Comment({
                "The number the player's frenzy level is multiplied by, before being clamped.",
                "In other words, this controls how many levels it takes to reach the maximum value,",
                "and what that maximum value is."
            })
            public double frenzyModifierScale = 1.0 / 20.0;
            
            @Name("FrenzyModifierMaxLevel")
            @Comment({
                "The maximum frenzy level allowed."
            })
            public int frenzyModifierMaxLevel = 15;
            
            @Name("ImpetusConductorFactor")
            @Comment("The amount the focus power is multiplied by when the gauntlet has a charged Impetus Conductor.")
            public double impetusConductorFactor = 1.1;
        }
        
        @Name("GauntletVisDiscounts")
        @Comment({
            "The discounts that will be applied to the vis cost of foci used in the thaumium and void metal caster gauntlets."
        })
        @RangeDouble(min = 0.0F, max = 1.0F)
        public double[] gauntletVisDiscounts = {0.1, 0.3};
        
        @Name("GauntletCooldownModifiers")
        @Comment({
            "The multipliers that will be applied to the use cooldowns of the Thaumium and Void Metal caster gauntlets."
        })
        @RangeDouble(min = 0.0F, max = 1.0F)
        public double[] gauntletCooldownModifiers = {0.80, 0.9};

        @Name("VoidseerExtraArea")
        @Comment({
            "The extra square area for the voidseer gauntlet, in chunks.",
            "An area of 3, for example, will mean vis will be taken in a 3x3 chunk area around the caster.",
            "Note that chunks still need to be loaded to take Vis from them, so the chunk load distance for your",
            "singleplayer/server is another limiting factor."
        })
        @RangeInt(min = 1, max = 32)
        public int voidseerArea = 3;

        @Name("VoidBootsLandSpeedBoost")
        @Comment({
            "The boost applied while the wearer is on the ground, and on dry land.",
            "This is added to the base movement of the player per tick."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsLandSpeedBoost = 0.09;

        @Name("VoidBootsWaterSpeedBoost")
        @Comment({
            "The boost applied while the wearer is in water.",
            "This is added to the base movement of the player per tick."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsWaterSpeedBoost = 0.045;

        @Name("VoidBootsJumpBoost")
        @Comment({
            "The boost applied when the wearer jumps.",
            "This is added to the base jump height of the player."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsJumpBoost = 0.4;

        @Name("VoidBootsJumpFactor")
        @Comment({
            "The boost applied to player movement while in the air.",
            "This itself is a speed, so it can make movement faster in the air than on the ground.",
            "Note that sprinting's jump modifier uses this value as well."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsJumpFactor = 0.03;

        @Name("VoidBootsStepHeight")
        @Comment({
            "The boost applied to the player's step height (while not sneaking).",
            "This is added to the vanilla default value of 0.6."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsStepHeight = 0.47;

        @Name("VoidBootsSneakReduction")
        @Comment({
            "Any speed boosts (not jump) will be divided by this value while sneaking."
        })
        @RangeDouble(min = 1.0F, max = 10.0F)
        public double voidBootsSneakReduction = 4.0F;
        
        @Name("ServerSideMovementCalculation")
        @Comment({
            "Makes the server calculate positions and velocities from the Boots of the Riftstrider in addition to the client.",
            "Normally the client is left to update their position, and the server just takes it from the client.",
            "Unless your server needs accurate position/velocity info for some reason, don't enable this."
        })
        public boolean serverMovementCalculation = false;
        
        @Name("DefaultCastingGauntletColors")
        @Comment({
            "The default dye colors for the thaumium and void gauntlets when crafted, in that order.",
            "The dyed color is multiplied with the color of the texture.",
            "This is a server-side setting."
        })
        public int[] defaultGauntletColors = new int[] {0x7A68C0, 0x262157};

        @Name("DefaultVoidBootsColor")
        @Comment({
            "The default dye color for the Boots of the Riftstrider when crafted.",
            "The dyed color is multiplied with the color of the texture.",
            "This is a server-side setting."
        })
        public int defaultVoidBootsColor = 0x6A3880;
        
        @Name("PrimalCutterDamage")
        @Comment({
            "The damage done by the Primal Cutter's material, NOT including the base damage.",
            "In other words, its total damage will be this number + 4 (3 for being a \"sword\", and 1 as the minimum)"
        })
        @RequiresMcRestart
        public float primalCutterDamage = 6.0F;
        
        @Name("DeniedResearchCategories")
        @Comment({
            "The list of research categories that Thaumic Augmentation will never willingly give research in.",
            "Sadly Thaumcraft makes all addons have a research category, even if they don't intend on using it.",
            "This is why cards like \"experiment\" can give categories that never appear otherwise.",
            "If you are a TC addon author and you want your mod included/removed from this list, message TheCodex6824 or open an issue/PR on Github."
        })
        public String[] deniedCategories = new String[] {
                "THAUMIC_AUGMENTATION",
                "THAUMIC_TINKERER",
                "THAUMIC_WONDERS",
                "RUSTIC_THAUMATURGY",
                "PERIPHERY",
                "ENGINEERED_GOLEMS",
                "EXPANDEDARCANUM",
                "MECHANICS"
        };
        
        @Name("AllowWussRiftSeed")
        @Comment("Allows rift seeds to create Flux Rifts, even if Thaumcraft's wuss mode is enabled.")
        public boolean allowWussRiftSeed = false;
        
    }
    
    public static class WorldOptions {
        
        @Name("EmptinessDimID")
        @Comment({
            "The dimension ID to use for the Emptiness dimension.",
            "If this ID is already taken, a new one will automatically be assigned."
        })
        @RequiresMcRestart
        public int emptinessDimID = 14676;

        @Name("EmptinessMoveFactor")
        @Comment({
            "The scaling factor applied to distances in the Emptiness dimension.",
            "For example, the nether has a value of 8 since it multiplies coords by 8.",
            "Note that move factors for the Emptiness are calculated based on chunk rather than position, so final values",
            "may be slightly different than expected."
        })
        public double emptinessMoveFactor = 16.0;

        @Name("FractureGenChance")
        @Comment({
            "The chance for a fracture to generate in a chunk in the Emptiness dimension.",
            "The approximate chance will be 1 / chance (assuming the chunk meets all other conditions).",
            "Set this to 0 to stop fractures from spawning completely, but be warned that there is no",
            "other way in Thaumic Augmentation to access the Emptiness in survival."
        })
        public int fractureGenChance = 35;
        
        @Name("FractureDimList")
        @Comment({
            "Lists the whitelisted dimensions for fractures (not including the Emptiness dim), and their associated weights.",
            "The format for each line is dim=weight, with higher weights (compared to lower weights) being more likely to spawn.",
            "This WILL affect worldgen, so use with caution on existing worlds.",
            "Default dimensions: 0 = Overworld, -1 = Nether, 1 = End, 7 = Twilight Forest, 17 = Atum 2,",
            "20 = Betweenlands, 111 = Lost Cities, 66 = Erebus, 33 = Wizardry (Underworld)"
        })
        public String[] fractureDimList = new String[] {
            "0=35",
            "-1=15",
            "1=10",
            "7=5", // twilight forest
            "17=5", // atum 2
            "20=5", // betweenlands
            "111=5", // lost cities
            "66=5" // erebus
        };
        
        @Name("FractureLocatorUpdateInterval")
        @Comment({
            "How often the location pointed to by the Fracture Locator should be updated, in milliseconds.",
            "This is a server-side setting."
        })
        public int fractureLocatorUpdateInterval = 2000;
        
        @Name("ValidFracturesAlwaysTeleport")
        @Comment({
            "If this is set, fractures that previously found a valid location will always teleport the player, even if it is now invalid.",
            "Normally, fractures check if there is a fracture at the destination to make sure players can get back.",
            "This is a server-side setting."
        })
        public boolean fracturesAlwaysTeleport = false;
        
        @Name("DisableEmptinessDimension")
        @Comment({
            "Completely disables the Emptiness dimension, *including* all fracture generation.",
            "This is not the intended way to experience the mod but is included here for modpack authors.",
            "This is a server-side setting, but will probably cause problems if the client does not have the same value."
        })
        @RequiresMcRestart
        public boolean disableEmptiness = false;
        
        @Name("GenerateEldritchSpires")
        @Comment({
            "Allows the Eldritch Spire structure to generate in the Emptiness.",
            "The spire is intended to be a part of the research progression, so removing it is not recommended.",
            "The structure will never generate if the Emptiness itself is disabled, or if the structure generation world option is disabled."
        })
        public boolean generateSpires = true;
        
        @Name("SpireMinDist")
        @Comment({
            "The absolute minimum distance, in chunks, between generated Eldritch Spires."
        })
        public int spireMinDist = 10;
        
        @Name("SpireSpacing")
        @Comment({
            "The approximate distance, in chunks, between generated Eldritch Spires."
        })
        public int spireSpacing = 30;
        
    }
    
    public static class ClientOptions {
        
        @Name("ReducedEffects")
        @Comment({
            "Disables some unneccessary particle effects.",
            "This includes the special effect of the casted light, as well as most of the particles on the Metaspatial Accumulator/Extruder"
        })
        public boolean reducedEffects = false;
        
        @Name("OptimizedFluxRiftRenderer")
        @Comment({
            "Overrides Thaumcraft's Flux Rift renderer to use one that is slightly better for performance.",
            "It probably won't make a major difference as of now, but can help.",
            "Further work may make this option more useful."
        })
        @RequiresMcRestart
        public boolean optimizedFluxRiftRenderer = false;
        
        @Name("GauntletCastAnimation")
        @Comment({
            "Enables a simple animation where an entity holds their arm out after casting.",
            "This is a client-side setting."
        })
        public boolean gauntletCastAnimation = true;
        
        @Name("EnableImpetusThrusterKeybind")
        @Comment({
            "Allows the Impetus Thruster boost key to be rebindable (with caveats).",
            "By default, the Impetus Thruster requires holding the jump key to activate it, and is not rebindable.",
            "This is required to not mess with other mods/content that checks for the jump key, as a normal keybind would conflict.",
            "However, if you'd rather not use jump for that, enabling this and restarting will make it appear as a normal keybind.",
            "To properly set it to jump again, you will need to disable this option and restart the game."
        })
        @RequiresMcRestart
        public boolean enableBoosterKeybind = false;
        
        @Name("DisableShaders")
        @Comment({
            "Disables all shaders used by TA for rendering.",
            "This will make certain things look ugly, but should prevent issues with shader mods.",
            "This does not change things for vanilla TC, except for flux rifts if \"OptimizedFluxRiftRenderer\" is enabled."
        })
        @RequiresMcRestart
        public boolean disableShaders = false;
        
        @Name("MorphicArmorExclusions")
        @Comment({
            "Due to the way Morphic Armor works, some armor items may not play nicely with it.",
            "This most commonly appears as the armor being invisible in the inventory.",
            "If that occurs, add its model name here (including variant names, if needed).",
            "Regular expressions are supported."
        })
        @RequiresMcRestart
        public String[] morphicArmorExclusions = new String[] {
            "extrabitmanipulation:moving_part*" // incompatible model retrieval method 
        };
        
        @Name("DisableCreativeOnlyText")
        @Comment({
            "Removes the \"Creative Only\" text on items that are creative-only normally.",
            "This is for modpack authors that might want to make these items obtainable."
        })
        public boolean disableCreativeOnlyText = false;
    }
    
    private static ArrayList<Runnable> listeners = new ArrayList<>();
    
    public static void addListener(Runnable r) {
        listeners.add(r);
    }
    
    public static boolean removeListener(Runnable r) {
        return listeners.remove(r);
    }
    
    public static Collection<Runnable> getListeners() {
        return listeners;
    }

    public static void loadConfigValues(Side side) {
        TAConfig.gauntletVisDiscounts.setValue(gameplay.gauntletVisDiscounts, side);
        TAConfig.gauntletCooldownModifiers.setValue(gameplay.gauntletCooldownModifiers, side);

        TAConfig.voidseerArea.setValue(gameplay.voidseerArea, side);

        TAConfig.voidBootsLandSpeedBoost.setValue(gameplay.voidBootsLandSpeedBoost, side);
        TAConfig.voidBootsWaterSpeedBoost.setValue(gameplay.voidBootsWaterSpeedBoost, side);
        TAConfig.voidBootsJumpBoost.setValue(gameplay.voidBootsJumpBoost, side);
        TAConfig.voidBootsJumpFactor.setValue(gameplay.voidBootsJumpFactor, side);
        TAConfig.voidBootsStepHeight.setValue(gameplay.voidBootsStepHeight, side);
        TAConfig.voidBootsSneakReduction.setValue(gameplay.voidBootsSneakReduction, side);
        TAConfig.serverMovementCalculation.setValue(gameplay.serverMovementCalculation, side);

        TAConfig.opWardOverride.setValue(gameplay.ward.opWardOverride, side);
        TAConfig.singlePlayerWardOverride.setValue(gameplay.ward.singlePlayerWardOverride, side);
        TAConfig.tileWardMode.setValue(gameplay.ward.tileWardMode, side);

        TAConfig.reducedEffects.setValue(client.reducedEffects, side);
        TAConfig.disableCreativeOnlyText.setValue(client.disableCreativeOnlyText, side);
        
        TAConfig.defaultGauntletColors.setValue(gameplay.defaultGauntletColors, side);
        TAConfig.defaultVoidBootsColor.setValue(gameplay.defaultVoidBootsColor, side);

        TAConfig.emptinessMoveFactor.setValue(world.emptinessMoveFactor, side);
        TAConfig.fractureGenChance.setValue(world.fractureGenChance, side);
        TAConfig.fractureLocatorUpdateInterval.setValue(world.fractureLocatorUpdateInterval, side);
        TAConfig.fracturesAlwaysTeleport.setValue(world.fracturesAlwaysTeleport, side);
        
        TAConfig.gauntletCastAnimation.setValue(client.gauntletCastAnimation, side);
        
        TAConfig.terraformerImpetusCost.setValue((long) gameplay.impetus.terraformerCost);
        TAConfig.shieldFocusImpetusCost.setValue((long) gameplay.impetus.shieldFocusCost);
        
        TAConfig.allowWussRiftSeed.setValue(gameplay.allowWussRiftSeed);
        
        TAConfig.cannonBeamDamage.setValue(gameplay.impetus.cannon.beamDamage);
        TAConfig.cannonBeamCost.setValue((long) gameplay.impetus.cannon.beamCost);
        TAConfig.cannonBeamRange.setValue(gameplay.impetus.cannon.beamRange);
        
        TAConfig.cannonRailgunDamage.setValue(gameplay.impetus.cannon.railgunDamage);
        TAConfig.cannonRailgunCost.setValue((long) gameplay.impetus.cannon.railgunCost);
        TAConfig.cannonRailgunCooldown.setValue(gameplay.impetus.cannon.railgunCooldown);
        TAConfig.cannonRailgunRange.setValue(gameplay.impetus.cannon.railgunRange);
        
        TAConfig.cannonBurstDamage.setValue(gameplay.impetus.cannon.burstDamage);
        TAConfig.cannonBurstCost.setValue((long) gameplay.impetus.cannon.burstCost);
        TAConfig.cannonBurstCooldown.setValue(gameplay.impetus.cannon.burstCooldown);
        TAConfig.cannonBurstRange.setValue(gameplay.impetus.cannon.burstRange);
        
        TAConfig.primalCutterDamage.setValue(gameplay.primalCutterDamage);
        
        TAConfig.deniedCategories.setValue(gameplay.deniedCategories);
        
        TAConfig.generateSpires.setValue(world.generateSpires);
        TAConfig.spireMinDist.setValue(world.spireMinDist);
        TAConfig.spireSpacing.setValue(world.spireSpacing);
        
        TAConfig.experienceModifierCap.setValue(gameplay.augment.experienceModifierCap);
        TAConfig.experienceModifierBase.setValue(gameplay.augment.experienceModifierBase);
        TAConfig.experienceModifierScale.setValue(gameplay.augment.experienceModifierScale);
    
        TAConfig.elementalModifierPositiveFactor.setValue(gameplay.augment.elementalModifierPositiveFactor);
        TAConfig.elementalModifierNegativeFactor.setValue(gameplay.augment.elementalModifierNegativeFactor);
    
        TAConfig.dimensionalModifierOverworldPostiveFactor.setValue(gameplay.augment.dimensionalModifierOverworldPositiveFactor);
        TAConfig.dimensionalModifierOverworldNegativeFactor.setValue(gameplay.augment.dimensionalModifierOverworldNegativeFactor);
        TAConfig.dimensionalModifierOverworldDims.setValue(ImmutableSet.copyOf(gameplay.augment.dimensionalModifierOverworldDims));
    
        TAConfig.dimensionalModifierNetherPostiveFactor.setValue(gameplay.augment.dimensionalModifierNetherPositiveFactor);
        TAConfig.dimensionalModifierNetherNegativeFactor.setValue(gameplay.augment.dimensionalModifierNetherNegativeFactor);
        TAConfig.dimensionalModifierNetherDims.setValue(ImmutableSet.copyOf(gameplay.augment.dimensionalModifierNetherDims));
        
        TAConfig.dimensionalModifierEndPostiveFactor.setValue(gameplay.augment.dimensionalModifierEndPositiveFactor);
        TAConfig.dimensionalModifierEndNegativeFactor.setValue(gameplay.augment.dimensionalModifierEndNegativeFactor);
        TAConfig.dimensionalModifierEndDims.setValue(ImmutableSet.copyOf(gameplay.augment.dimensionalModifierEndDims));
        
        TAConfig.dimensionalModifierEmptinessPostiveFactor.setValue(gameplay.augment.dimensionalModifierEmptinessPositiveFactor);
        TAConfig.dimensionalModifierEmptinessNegativeFactor.setValue(gameplay.augment.dimensionalModifierEmptinessNegativeFactor);
        TAConfig.dimensionalModifierEmptinessDims.setValue(ImmutableSet.copyOf(gameplay.augment.dimensionalModifierEmptinessDims));
    
        TAConfig.frenzyModifierScaleFactor.setValue(gameplay.augment.frenzyModifierScale);
        TAConfig.frenzyModifierCooldown.setValue(gameplay.augment.frenzyModifierCooldown);
        TAConfig.frenzyModifierMaxLevel.setValue(gameplay.augment.frenzyModifierMaxLevel);
        
        TAConfig.impetusConductorFactor.setValue(gameplay.augment.impetusConductorFactor);
    }

    public static void syncLocally() {
        ConfigManager.sync(ThaumicAugmentationAPI.MODID, Type.INSTANCE);
    }

    public static void syncConfig() {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
            syncConfig(player);
    }

    public static void syncConfig(EntityPlayerMP target) {
        TANetwork.INSTANCE.sendTo(new PacketConfigSync(TAConfigManager.createSyncBuffer(Side.CLIENT)), target);
    }

    public static void loadOrSyncConfig(EntityPlayer player) {
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            loadConfigValues(Side.SERVER);
            syncConfig((EntityPlayerMP) player);
        }
        else if (!player.world.isRemote)
            loadConfigValues(Side.SERVER);
        else
            loadConfigValues(Side.CLIENT);
    }

    public static void preInit() {
        TAConfig.gauntletVisDiscounts = TAConfigManager.addOption(new ConfigOptionDoubleList(false, gameplay.gauntletVisDiscounts));
        TAConfig.gauntletCooldownModifiers = TAConfigManager.addOption(new ConfigOptionDoubleList(false, gameplay.gauntletCooldownModifiers));

        TAConfig.voidseerArea = TAConfigManager.addOption(new ConfigOptionInt(false, gameplay.voidseerArea));

        TAConfig.voidBootsLandSpeedBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsLandSpeedBoost));
        TAConfig.voidBootsWaterSpeedBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsWaterSpeedBoost));
        TAConfig.voidBootsJumpBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsJumpBoost));
        TAConfig.voidBootsJumpFactor = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsJumpFactor));
        TAConfig.voidBootsStepHeight = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsStepHeight));
        TAConfig.voidBootsSneakReduction = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsSneakReduction));
        TAConfig.serverMovementCalculation = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.serverMovementCalculation));
        
        TAConfig.opWardOverride = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.ward.opWardOverride));
        TAConfig.singlePlayerWardOverride = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.ward.singlePlayerWardOverride));
        TAConfig.disableWardFocus = TAConfigManager.addOption(new ConfigOptionBoolean(true, gameplay.ward.disableWardFocus));
        TAConfig.tileWardMode = TAConfigManager.addOption(new ConfigOptionEnum<>(false, gameplay.ward.tileWardMode, new IEnumSerializer<TileWardMode>() {
           
            @Override
            public void serialize(TileWardMode value, ByteBuf buf) {
                buf.writeInt(value.ordinal());
            }
            
            @Override
            public TileWardMode deserialize(ByteBuf buf) {
                return TileWardMode.values()[Math.min(TileWardMode.values().length - 1, Math.max(buf.readInt(), 0))];
            }
            
        }));
        
        TAConfig.reducedEffects = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.reducedEffects));
        TAConfig.optimizedFluxRiftRenderer = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.optimizedFluxRiftRenderer));
        TAConfig.enableBoosterKeybind = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.enableBoosterKeybind));
        TAConfig.disableShaders = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.disableShaders));
        TAConfig.morphicArmorExclusions = TAConfigManager.addOption(new ConfigOptionStringList(false, client.morphicArmorExclusions));
        TAConfig.disableCreativeOnlyText = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.disableCreativeOnlyText));
        
        TAConfig.defaultGauntletColors = TAConfigManager.addOption(new ConfigOptionIntList(true, gameplay.defaultGauntletColors));
        TAConfig.defaultVoidBootsColor = TAConfigManager.addOption(new ConfigOptionInt(true, gameplay.defaultVoidBootsColor));

        TAConfig.emptinessDimID = TAConfigManager.addOption(new ConfigOptionInt(true, world.emptinessDimID));
        TAConfig.emptinessMoveFactor = TAConfigManager.addOption(new ConfigOptionDouble(true, world.emptinessMoveFactor));
        TAConfig.fractureGenChance = TAConfigManager.addOption(new ConfigOptionInt(false, world.fractureGenChance));
        
        TAConfig.fractureDimList = TAConfigManager.addOption(new ConfigOptionStringList(false, world.fractureDimList));
        TAConfig.fractureLocatorUpdateInterval = TAConfigManager.addOption(new ConfigOptionInt(false, world.fractureLocatorUpdateInterval));
        TAConfig.fracturesAlwaysTeleport = TAConfigManager.addOption(new ConfigOptionBoolean(false, world.fracturesAlwaysTeleport));
        TAConfig.disableEmptiness = TAConfigManager.addOption(new ConfigOptionBoolean(false, world.disableEmptiness));
    
        TAConfig.disableCoremod = TAConfigManager.addOption(new ConfigOptionBoolean(false, general.disableCoremod));
        TAConfig.disabledTransformers = TAConfigManager.addOption(new ConfigOptionStringList(false, general.disabledTransformers));
        
        TAConfig.gauntletCastAnimation = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.gauntletCastAnimation));
    
        TAConfig.terraformerImpetusCost = TAConfigManager.addOption(new ConfigOptionLong(false, (long) gameplay.impetus.terraformerCost));
        TAConfig.shieldFocusImpetusCost = TAConfigManager.addOption(new ConfigOptionLong(false, (long) gameplay.impetus.shieldFocusCost));
    
        TAConfig.allowWussRiftSeed = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.allowWussRiftSeed));
        
        TAConfig.cannonBeamDamage = TAConfigManager.addOption(new ConfigOptionFloat(false, gameplay.impetus.cannon.beamDamage));
        TAConfig.cannonBeamCost = TAConfigManager.addOption(new ConfigOptionLong(true, (long) gameplay.impetus.cannon.beamCost));
        TAConfig.cannonBeamRange = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.impetus.cannon.beamRange));
        
        TAConfig.cannonRailgunDamage = TAConfigManager.addOption(new ConfigOptionFloat(false, gameplay.impetus.cannon.railgunDamage));
        TAConfig.cannonRailgunCost = TAConfigManager.addOption(new ConfigOptionLong(true, (long) gameplay.impetus.cannon.railgunCost));
        TAConfig.cannonRailgunCooldown = TAConfigManager.addOption(new ConfigOptionInt(true, gameplay.impetus.cannon.railgunCooldown));
        TAConfig.cannonRailgunRange = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.impetus.cannon.railgunRange));
        
        TAConfig.cannonBurstDamage = TAConfigManager.addOption(new ConfigOptionFloat(false, gameplay.impetus.cannon.burstDamage));
        TAConfig.cannonBurstCost = TAConfigManager.addOption(new ConfigOptionLong(true, (long) gameplay.impetus.cannon.burstCost));
        TAConfig.cannonBurstCooldown = TAConfigManager.addOption(new ConfigOptionInt(true, gameplay.impetus.cannon.burstCooldown));
        TAConfig.cannonBurstRange = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.impetus.cannon.burstRange));
    
        TAConfig.primalCutterDamage = TAConfigManager.addOption(new ConfigOptionFloat(false, gameplay.primalCutterDamage));
    
        TAConfig.deniedCategories = TAConfigManager.addOption(new ConfigOptionStringList(true, gameplay.deniedCategories));
    
        TAConfig.generateSpires = TAConfigManager.addOption(new ConfigOptionBoolean(false, world.generateSpires));
        TAConfig.spireMinDist = TAConfigManager.addOption(new ConfigOptionInt(false, world.spireMinDist));
        TAConfig.spireSpacing = TAConfigManager.addOption(new ConfigOptionInt(false, world.spireSpacing));
    
        TAConfig.experienceModifierCap = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.experienceModifierCap));
        TAConfig.experienceModifierBase = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.experienceModifierBase));
        TAConfig.experienceModifierScale = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.experienceModifierScale));
    
        TAConfig.elementalModifierPositiveFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.elementalModifierPositiveFactor));
        TAConfig.elementalModifierNegativeFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.elementalModifierNegativeFactor));
    
        TAConfig.dimensionalModifierOverworldPostiveFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierOverworldPositiveFactor));
        TAConfig.dimensionalModifierOverworldNegativeFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierOverworldNegativeFactor));
        TAConfig.dimensionalModifierOverworldDims = TAConfigManager.addOption(new ConfigOptionIntSet(false, ImmutableSet.copyOf(gameplay.augment.dimensionalModifierOverworldDims)));
    
        TAConfig.dimensionalModifierNetherPostiveFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierNetherPositiveFactor));
        TAConfig.dimensionalModifierNetherNegativeFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierNetherNegativeFactor));
        TAConfig.dimensionalModifierNetherDims = TAConfigManager.addOption(new ConfigOptionIntSet(false, ImmutableSet.copyOf(gameplay.augment.dimensionalModifierNetherDims)));
        
        TAConfig.dimensionalModifierEndPostiveFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierEndPositiveFactor));
        TAConfig.dimensionalModifierEndNegativeFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierEndNegativeFactor));
        TAConfig.dimensionalModifierEndDims = TAConfigManager.addOption(new ConfigOptionIntSet(false, ImmutableSet.copyOf(gameplay.augment.dimensionalModifierEndDims)));
        
        TAConfig.dimensionalModifierEmptinessPostiveFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierEmptinessPositiveFactor));
        TAConfig.dimensionalModifierEmptinessNegativeFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.dimensionalModifierEmptinessNegativeFactor));
        TAConfig.dimensionalModifierEmptinessDims = TAConfigManager.addOption(new ConfigOptionIntSet(false, ImmutableSet.copyOf(gameplay.augment.dimensionalModifierEmptinessDims)));
    
        TAConfig.frenzyModifierScaleFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.frenzyModifierScale));
        TAConfig.frenzyModifierCooldown = TAConfigManager.addOption(new ConfigOptionInt(false, gameplay.augment.frenzyModifierCooldown));
        TAConfig.frenzyModifierMaxLevel = TAConfigManager.addOption(new ConfigOptionInt(false, gameplay.augment.frenzyModifierMaxLevel));
        
        TAConfig.impetusConductorFactor = TAConfigManager.addOption(new ConfigOptionDouble(false, gameplay.augment.impetusConductorFactor));
    }

}
