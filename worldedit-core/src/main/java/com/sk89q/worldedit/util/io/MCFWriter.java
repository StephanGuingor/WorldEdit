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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MCFWriter {


    public static void writeToDataPack(File baseDir, String hologramName, ArrayList<String[]> data,int timeHolo,int deltaTime) {

        hologramName = hologramName.toLowerCase().trim();


        // Create Datapack structure
        File newHologramDirectory = createDatapackBaseStructure(baseDir,hologramName);

        //Making new folder directory title the Hologram Name
        File newDir = new File(newHologramDirectory.getPath()+File.separator+hologramName);
        newDir.mkdir();

        //Making setblock directory (this one will hold all mcfunctions with setblock command)
        File setblockDir = new File(newDir.getPath()+File.separator+"setblocks");
        setblockDir.mkdir();

        //Appending runner script to hologram
        appendToHologramManager(newHologramDirectory, hologramName);

        //Write Init File
        writeInitFile(newDir, hologramName);

        //Write remaining files
        writeRunnerAndHelperFiles(newDir, setblockDir, hologramName, data,timeHolo,deltaTime);
    }

    public static File createDatapackBaseStructure(File baseDir,String datapackName){

        // Creating Base Folder
        File newDir = new File(baseDir.getPath()+File.separator+datapackName);
        newDir.mkdir();


        try {
            // Creating PackMC Meta
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(newDir.getPath() + File.separator + "pack.mcmeta", false)));
            fw.println("{\"pack\": {\"pack_format\": 6,\"description\": \"Datapack created using Vanillafy by Pseudo Elephant: https://www.youtube.com/channel/UC3Q20nzJ-n2e_ilzLflR1ew, \"}}");
            fw.flush();
            fw.close();
        } catch (IOException e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

        // Data Directory
        newDir = new File(newDir.getPath()+File.separator+"data");
        newDir.mkdir();

        String namespaceDir = newDir.getPath();
        newDir = new File(namespaceDir+File.separator+"minecraft"+File.separator+"tags"+File.separator+"functions");
        newDir.mkdirs();

        try {
            // Creating Tick Json
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(newDir.getPath() + File.separator + "tick.json", false)));
            fw.println("{\"values\": [\"" + datapackName+":hologram/" + "hologram_manager\"]}");
            fw.flush();
            fw.close();

        }   catch (IOException e){
            System.out.println(e.getStackTrace());
        }

        newDir = new File(namespaceDir+File.separator+datapackName+File.separator+"functions"+File.separator+"hologram");
        newDir.mkdirs();

        return newDir;

    }

    public static void writeRunnerAndHelperFiles(File newDir, File setblocksDir, String hologramName, ArrayList<String[]> data,int timeHolo,int deltaTime) {
        //Function to place Blocks
        int blocks = data.size();
        int blockToPlace = 1;
        int maxTime = timeHolo; //In Ticks!
        int maxBlocksPerIteration = (2*blocks)/maxTime;

        System.out.println("b = "+maxBlocksPerIteration);

        //Create Runner File
        File runnerFile = new File(newDir.getPath()+File.separator+hologramName+"_runner.mcfunction");

        try {
            //Creating Print Writer
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(runnerFile.getPath(), false)));

            //Appending End Statement of Loop
            fw.println("execute if score dummy_player "+hologramName+"_bool matches 1 if score dummy_player "+hologramName+"_time matches " +maxTime*deltaTime+ " run scoreboard players set dummy_player "+hologramName+"_bool 0");
            fw.println("execute if score dummy_player "+hologramName+"_bool matches 0 run scoreboard players set dummy_player "+hologramName+"_time 0\n");

            int counter = 0;
            //For Loop to Create SetBlock files related to each iteration (and writing to runner the proper file execution code)
            for (int i = 0; i < maxTime; i++) {
                //Blocks to Place says how many blocks in that iteration
                blockToPlace = (int)Math.ceil(-(maxBlocksPerIteration/2.0)*Math.cos((2*Math.PI*i)/maxTime)+maxBlocksPerIteration/2.0);

                //Creating Files Helper File For Given Iteration
                File setblockFile = new File(setblocksDir.getPath()+"/"+hologramName+"_it_"+i+".mcfunction");
                PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(setblockFile.getPath(), false)));
                bw.println("### Datapack created using Vanillafy by Pseudo Elephant: https://www.youtube.com/channel/UC3Q20nzJ-n2e_ilzLflR1ew");
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
                fw.println("execute if score dummy_player "+hologramName+"_bool matches 1 if score dummy_player "+hologramName+"_time matches "+ i*deltaTime +" run function " + hologramName+":hologram/"  + hologramName +"/setblocks"+"/"+hologramName+"_it_"+i);
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
            fw.println("execute if score dummy_player " +hologramName + "_bool " + "matches 1 run function " + hologramName+":hologram/"  + hologramName +"/" + hologramName + "_runner");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<String[]> shuffleDataCluster(ArrayList<String[]> data) {
        ArrayList<String[]> shuffledList = new ArrayList<>();
        ArrayList<ArrayList<String[]>> clusters = getClusters(data);
        for (ArrayList<String[]> cluster: clusters) {
            Collections.shuffle(cluster);
            shuffledList.addAll(cluster);
        }

        return shuffledList;
    }

    public static ArrayList<String[]> shuffleData(ArrayList<String[]> data) {
        Collections.shuffle(data);
        return data;
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
