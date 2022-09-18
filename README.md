# Villainous Card Photocopier

An automatic Card Photocopier that takes your card images, once generated by the Villainous Card Generator, then reads the information in your Villainous Template.ods to duplicate all cards accordingly and generate a TTS deck-ready image to be used in a TTS custom deck. All it takes is 1 minutes of preparation, after that everything is done in 1 click.

## Ok, so what does this do?

My small tool is going to take **ALL** the images in a folder (The -Exports folder for the Villain Card Generator is the recommended one), divide them into Fate or Villain decks (according to columns B "name of the card" and K "Villain/Fate" of the .ods file), multiply them according to the number of copies (taken from column A in the .ods file) and compile each deck into a light .jpg image that is ready to use in Tabletop Simulator's decks. **You will no longer need to use the TTS Deck Builder**, you will just need to **save your .ods, generate the images of your Villain, and double click this .jar file**. **Please follow the Instalation instructions**, thank you.

## Contact

If you have **ANY** doubt regarding how to install it or how to configure it or how to use it or any error you are seeing or any problem you are having, **no matter how dumb you think your question is**, please ask me. You can't compete dumb against me, you are no match to my superior dumbness. I'll be more than happy to resolve you any doubt or issue regarding the instalation or use of my tool or anything related. I might not see your message if you open an issue here on GitHub, so I recommend contacting me through discord (Cristichi#5193) or by email (cristichi@hotmail.es) (PLEASE STATE FIRST THAT YOU NEED HELP WITH MY CARD PHOTOCOPIER TOOL, otherwise I might missinterpret it by spam but I want to help you).

## Instalation

1. You need to be using the **Villainous Card Generator** (https://drive.google.com/drive/u/0/folders/1wNyGRrege46Kob-1tvYbb8eixMeOFf81). If you are not, **I highly recommend using it** along with my tool.
2. You need **Java 1.8 or superior** installed. If you are not sure if you have it installed, just keep following the instructions and at the end if you can't open the .jar with a double click, you need to install Java. After you install Java it should work.
3. Click here to **download the executable .jar file**: https://github.com/Cristichi/Villainous-CardPhotocopier/releases/tag/main-release
<br>You may download the source code if you are a Java nerd so you can modify it in any way. Feel free to modify it or ask me if you have any specific needs.
4. Place it in the folder where you are managing one of your villains. I recommend to copy this .jar file into a new folder dedicated to my tool for each of your villains. Organize it the way you desire, tho.
5. **Double click it so it generates the config.txt file**. Please don't move that file or rename it or it will be generated again if my tool can't find it.
<br>5.1. You will see that the first line of the config file is ignored.
<br>5.2. On the second line, you will have to put **the path to your -Exports file or where you have the images of your villain**. ***THIS FOLDER MUST CONTAIN ONLY THE IMAGES OF THE VILLAIN YOU ARE GOING TO USE MY TOOL FOR***. This is very important, because my tool is going to use these files as base for the cards that need to be used.
<br>5.3. On the third line is going to be **the name or path of the folder** where the images are going to be saved. Just leave "Results" as it is or personalize it to reflect your evil impulses with something like like "My minions".
<br>5.4. On the fouth line, very important, there needs to be *the path to a .ods file* (very important that it's an .ods file, otherwise it won't know how to read it) **that follows the same columns as the default *"Villainous Template.ods"***. It MUST include at least the cards for your villain, but it doesn't really matter if it contains other villains. You can make the process slightly faster if you remove all other villains but it's hardly worth it.
<br>5.5. On the fifth line, **you may leave "Autoclose"** (uppercase/lowercase are ignored, you may write "aUtOclOsE" if you want) so it closes automatically if everything was OK. I recommend doing it in 100% of situations, unless you don't trust me. Please somebody trust me.
<br>If you want to close the final OK screen manually, you are free to *remove or erase that line*.
6. **You are done!** Test it with one of your Villains that are already finished and doing their evil schemes.

## Usage

You may not use this program to generate a 3rd deck that is neither the Villain nor Fate deck. But **you still may use my tool for that Villain's Fate and Villain deck normally**, to do so just make sure that the value in the K column of the .ods for the cards that are neither in the Fate nor Villain decks is not a valid value to be interpreted as either Fate nor Villain ("**0**", "**1**", "**Villain**" or "**Fate**", without the quotation marks of course).

If you followed the instruction, **just make sure your .ods file is saved** and **it has the A column filled with the number of copies for each card**, as well as the information of each card, **and double click in the .jar file** you downloaded and it will open a window telling you what it's currently doing. Not that you need to know, but it's great to have I guess.

* If everything was done perfectly, it will show you a message telling you that everything was OK and the number of Fate and Villain cards was the expected (15 and 30 respectively). Then after a second it will autoclose if you left the Autoclose line in the configuration file. Go to the Result folder (or whatever you named it in the config) and there they are, they 2 images with all the cards and all their copies.
* If everything was OK but you are using a weird number of Fate or Villain cards, it will tell you as well (it will also prevent the window from autoclose). The result would be there in case you don't want your Fate/Villain decks to have the usual number of cards. The warning is great tho, because sometimes I forget to save the .ods after some changes.
* Otherwise, if there was an error, **please resolve the problem that you are shown*, *like fixing the config if there is something wrong or paths leading to nowhere*, or if you do't seem to understand the problem, or they it just makes no sense, **please contact me on discord or email** and I'll be so happy to help you. (it will also prevent it form autoclosing so that you can read it carefully or screenshot it)

There is also a special usage for it. If you are using a special Fate deck that requires you to generate your Fate cards as Villain cards with an alternate layout (for example, when you need to add a cost to your Fate cards), you can still mark them as Fate cards for my plugin. You will need to add a new row after all Villain cards and before all Fate cards, combine that row's cells from A to M into 1 cell *(actually, only the A and B column are needed to be combined, but combining the whole row seems more elegant)*, and write "**- Fate -**" (without the quotation marks) and my program will interpret every single card behind it as a Fate card until it finds another combined cell *(again, one that has at least A and B columns combined. This time it doesn't require something to be written)*.

## How to update

I won't do an automatic updater, but you can always check this page again for an update. To update is quite easy:

1. **Download the lastest release* on this same GitHub project.
2. **Substitute the outdated .jar** file with the new one you just downloaded.
3. Done!

## The future

I might want to add new configuration values for the name of the Villain and Fate decks that are generated.

Also, please contact me if you have any request.
