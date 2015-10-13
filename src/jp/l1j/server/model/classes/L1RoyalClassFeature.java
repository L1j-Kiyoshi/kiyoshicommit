/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.l1j.server.model.classes;

import jp.l1j.configure.Config;

class L1RoyalClassFeature extends L1ClassFeature {
        @Override
        public int getAcDefenseMax(int ac) {
                return ac / 3;
        }

        @Override
        public int getMagicLevel(int playerLevel) {
                return Math.min(2, playerLevel / 10);
        }

        @Override
        public String getClassNameInitial() {
                return "P";
        }

        @Override
        public int getMaxSpellLevel() {
                return Config.PRINCE_LEARN_MAGIC_MAX_CLASS;
        }

        @Override
        public int getSpellLearningInterval() {
                return Config.PRINCE_LEARN_MAGIC_PER_LEVEL;
        }
}