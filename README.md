# Villainous Card Photocopier

An automatic Card Photocopier that takes your card images, once generated by the Villainous Card Generator, then reads the information in your Villainous Template.ods to duplicate all cards accordingly and generate a TTS deck-ready image to be used in a TTS custom deck. All it takes is 1 minutes of preparation, after that everything is done in 1 click.

## Ok, so what does this do?

My small tool is going to take **ALL** the images in a folder (The -Exports folder for the Villain Card Generator, for example, or a folder where you'll put the images of all your villain's Villain and Fate cards), divide them into Fate or Villain decks (according to columns B "name of the card" and K "Villain/Fate" of the .ods file), multiply them according to the number of copies (taken from column A in the .ods file) and compile each deck into a light .jpg image that is ready to use in Tabletop Simulator's decks. **You will no longer need to use the TTS Deck Builder**, you will just need to **save your .ods, generate the images of your Villain, and double click this .jar file**.

## Contact

If you have **ANY** doubt regarding how to install it or how to configure it or how to use it or any error you are seeing or any problem you are having, **no matter how dumb you think your question is**, please ask me. You can't compete dumb against me, you are no match to my superior dumbness. I'll be more than happy to resolve any question or issue regarding the instalation or use of my tool. I might not see your message if you open an issue here on GitHub, so I recommend contacting me through discord (Cristichi#5193) or by email (cristichi@hotmail.es) (PLEASE STATE FIRST THAT YOU NEED HELP WITH MY CARD PHOTOCOPIER TOOL, otherwise I might missinterpret it as spam but I want to help you). I'll contact you as soon as possible in my free time.

## Instalation

1. You need to be using the **Villainous Card Generator** (https://drive.google.com/drive/u/0/folders/1wNyGRrege46Kob-1tvYbb8eixMeOFf81). If you are not, **I highly doubt my tool will be useful to you** because it reads the .ods file that it provides to read information about each card (number of copies and the deck it belongs to). If for some reason you are not, you need to create a .ods file where you put the number of copies in the A column, the exact name of the card in column B and "**Villain**"/"**0**" or "**Fate**"/"**1**" on the column K and you are good to go.
2. You need **Java** installed. If you are not sure if you have it installed, just keep following the instructions and at the end if you can't open the .jar with a double click, you need to install Java. After you install Java it should work.
3. Click here to **download the executable .jar file**: [https://github.com/Cristichi/Villainous-CardPhotocopier/releases/tag/main-release-v2.1](https://github.com/Cristichi/Villainous-CardPhotocopier/releases/tag/main-release-v2.1)
<br>You may download the source code if you are a Java nerd so you can modify it in any way. Feel free to modify it or ask me if you have any specific needs.
4. Place it in the folder where you are managing one of your villains. I recommend to copy this .jar file into a new folder dedicated to my tool for each of your villains. Organize it the way you desire, tho.
5. **Double click it so it generates the config.yml file**. Please don't move that file or rename it or it will be generated again if my tool can't find it.
6. Now close the window and open the config.yml file. Edit it. You may use the absolute path to the files and folders or a reltive path from where you will use my tool.
7. **You are done!** Test it with one of your Villains that are already finished and doing their evil schemes.

## Usage

You may not use this program to generate a 3rd deck that is neither the Villain nor Fate deck. But **you still may use my tool for that Villain's Fate and Villain deck normally**, to do so just make sure that the value in the A column of the .ods (Card count) for the cards that are neither in the Fate nor Villain decks is "**0**".

If you followed the instructions, **just make sure your .ods file is saved** and **it has the A column filled with the number of copies for each card**, as well as the information of each card, **and double click in the .jar file** you downloaded and it will open a window telling you what it's currently doing. Also note that everything must be in teh first page of the .ods file. If you need support for other pages, just tell me. It hasn't been done yet because I think everyone would be using the first page only.

1. To start you need to make sure all cards of your Villain, both Villain an Fate cards, are in the -Exports folder (or whatever folder you configured in the 2nd line of the configuration) and in the .ods file you specified in the 4th line of the configuration.
2. Then, make sure that no cards from other villains are present in both the folder and the .ods file as well, because them my tool will think they are all the same.
3. Double click the .jar file and there you go.

* If everything was done perfectly, it will show you a message telling you that everything was OK. Then after half a second it will autoclose (Autoclose is true in the configuration). Go to the Result folder (or whatever you named it in the config) and there they are, they 2 images with all the cards and all their copies.
* If everything was OK but you are using a weird number of Fate or Villain cards, it will tell you as well (it will also prevent the window from autoclosing). The result would be there in case you don't want your Fate/Villain decks to have the usual number of cards. The warning is great tho, because sometimes I forget to save the .ods after some changes or I didn't count the number of copies very well.
* Otherwise, if there was an error, **please resolve the problem that you are shown**, *like fixing the config if there is something wrong or paths leading to nowhere*, or if you don't seem to see the problem, or they it just makes no sense, **please contact me on discord or email** and I'll be so happy to help you or note the bug to fix it. (it will also prevent it form autoclosing so that you can read it carefully or screenshot it)

There is also a special usage for it. If you are using a special Fate deck that requires you to generate your Fate cards as Villain cards with an alternate layout (for example, when you need to add a cost to your Fate cards), you can still mark them as Fate cards for my plugin. You will need to add a new row after all Villain cards and before all Fate cards, combine that row's cells from A to M into 1 cell *(actually, only the A and B column are needed to be combined, but combining the whole row seems more elegant)*, and write "**- Fate -**" (without the quotation marks) and my program will interpret every single card behind it as a Fate card until it finds another combined cell *(again, one that has at least A and B columns combined. This time it doesn't require something to be written)*.

## How to update

I won't do an automatic updater, but you can always check this page again for an update. To update is quite easy:

1. **Download the lastest release** on this same GitHub project.
2. **Substitute the outdated .jar** file with the new one you just downloaded.
3. Done!

## The future

I might want to add new configuration values for the name of the Villain and Fate decks that are generated.

Also, please contact me if you have any request.
