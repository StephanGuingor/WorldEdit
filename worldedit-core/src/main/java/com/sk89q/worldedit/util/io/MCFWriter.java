/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class MCFWriter {
    //Attributes
    static final String hologramExt = "the_hand:hologram/";

    public static void writeToDataPack(File hologramDir, String hologramName, ArrayList<String[]> data) {
        hologramName = hologramName.toLowerCase().trim();

        //Making new folder directory title the Hologram Name
        File newDir = new File(hologramDir.getPath()+File.separator+hologramName);
        newDir.mkdir();

        //Making setblock directory (this one will hold all mcfunctions with setblock command)
        File setblockDir = new File(newDir.getPath()+File.separator+"setblocks");
        setblockDir.mkdir();

        //Appending runner script to hologram
        appendToHologramManager(hologramDir, hologramName);

        //Write Init File
        writeInitFile(newDir, hologramName);

        //Write remaining files
        writeRunnerAndHelperFiles(newDir, setblockDir, hologramName, data);
    }

    public static void writeRunnerAndHelperFiles(File newDir, File setblocksDir, String hologramName, ArrayList<String[]> data) {
        //Function to place Blocks
        int blocks = data.size();
        int blockToPlace = 1;
        int maxTime = 100; //In Ticks!
        int maxBlocksPerIteration = (2*blocks)/maxTime;

        System.out.println("b = "+maxBlocksPerIteration);

        //Create Runner File
        File runnerFile = new File(newDir.getPath()+File.separator+hologramName+"_runner.mcfunction");

        try {
            //Creating Print Writer
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(runnerFile.getPath(), false)));

            //Appending End Statement of Loop
            fw.println("execute if score dummy_player "+hologramName+"_bool matches 1 if score dummy_player "+hologramName+"_time matches " +maxTime+ " run scoreboard players set dummy_player "+hologramName+"_bool 0");
            fw.println("execute if score dummy_player "+hologramName+"_bool matches 0 run scoreboard players set dummy_player "+hologramName+"_time 0\n");

            int counter = 0;
            //For Loop to Create SetBlock files related to each iteration (and writing to runner the proper file execution code)
            for (int i = 0; i < maxTime; i++) {
                //Blocks to Place says how many blocks in that iteration
                blockToPlace = (int)Math.ceil(-(maxBlocksPerIteration/2.0)*Math.cos((2*Math.PI*i)/maxTime)+maxBlocksPerIteration/2.0);

                //Creating Files Helper File For Given Iteration
                File setblockFile = new File(setblocksDir.getPath()+"/"+hologramName+"_it_"+i+".mcfunction");
                PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(setblockFile.getPath(), false)));
                while(blockToPlace>0 && blocks>0) {
                    String coordinates = data.get(counter)[0].replaceAll("[,()]","");
                    String block = data.get(counter)[1];
                    bw.println("setblock "+ coordinates + " " + block);

                    blocks--;
                    blockToPlace--;
                    counter++;
                }
                bw.flush();
                bw.close();

                //Appending to Runner File
                fw.println("execute if score dummy_player "+hologramName+"_bool matches 1 if score dummy_player "+hologramName+"_time matches "+ i +" run function " + hologramExt + hologramName +"/setblocks"+"/"+hologramName+"_it_"+i);
            }

            fw.println("\nexecute if score dummy_player "+hologramName+"_bool matches 1 run scoreboard players add dummy_player "+hologramName+"_time 1");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void writeInitFile(File dir, String hologramName) {
        try {
            File initFile = new File(dir.getPath()+File.separator+hologramName+"_init.mcfunction");
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(initFile.getPath(), false)));
            fw.println("scoreboard objectives add " + hologramName + "_bool" + " dummy");
            fw.println("scoreboard objectives add " + hologramName + "_time" + " dummy\n");
            fw.println("scoreboard players set dummy_player "+hologramName+"_bool 1");
            fw.println("scoreboard players set dummy_player "+hologramName+"_time 0");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void appendToHologramManager(File dir, String hologramName) {
        File newFile = new File(dir.getPath()+File.separator+"hologram_manager.mcfunction"); //Hologram manager file
        hologramName = hologramName.toLowerCase().trim();

        try {
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(newFile.getPath(),true))); //Create new one
            fw.println("execute if score dummy_player " +hologramName + "_bool " + "matches 1 run function " + hologramExt + hologramName +"/" + hologramName + "_runner");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<String[]> shuffleData(ArrayList<String[]> data) {
        ArrayList<String[]> shuffledList = new ArrayList<>();
        ArrayList<ArrayList<String[]>> clusters = getClusters(data);
        for (ArrayList<String[]> cluster: clusters) {
            Collections.shuffle(cluster);
            shuffledList.addAll(cluster);
        }

        return shuffledList;
    }

    public static ArrayList<ArrayList<String[]>> getClusters(ArrayList<String[]> data){
        ArrayList<ArrayList<String[]>> clusters = new ArrayList<ArrayList<String[]>>();

        String prevy = "";
        String currenty; //Get current Y value

        for (int i = 0; i < data.size(); i++){
            currenty = data.get(i)[0].split(",")[1].trim(); //Y value
            if (!currenty.equals(prevy)) { //if Y value is different
                clusters.add(new ArrayList<String[]>()); //Create new arraylist
                clusters.get(clusters.size()-1).add(data.get(i)); //Add element to that cluster
                prevy = currenty;   //Prev is now current.
            }
            else {
                clusters.get(clusters.size()-1).add(data.get(i));
            }
        }

        return clusters;
    }
}
