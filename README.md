# Villainous-CardPhotocopier

An automatic Card Photocopier that takes your card images, once generated by the Villainous Card Generator, then reads the information in your Villainous Template.ods to duplicate all cards accordingly and generate a TTS deck-ready image to be used in a TTS custom deck. All it takes is 1 minutes of preparation, after that everything is done in 1 click.

## Contact

If you have ANY doubt regarding how to install it or how to configure it or how to use it or any error you are seeing or any problem you are having, no matter how dumb you think your question is, please ask me. You can't compete dumb against me, you are no match to my superior dumbness. I'll be more than happy to resolve you any doubt or issue regarding the instalation or use of my tool or anything related. I might not see your message if you open an issue here on GitHub, so I recommend contacting me through discord (Cristichi#5193) or by email (cristichi@hotmail.es) (PLEASE STATE FIRST THAT YOU NEED HELP WITH MY CARD PHOTOCOPIER TOOL, otherwise I might missinterpret it by spam but I want to help you).

## Instalation

1. You need to be using the Villainous Card Generator (https://drive.google.com/drive/u/0/folders/1wNyGRrege46Kob-1tvYbb8eixMeOFf81). If you are not, I highly recommend using it along with my tool or the TTS Deck Builder (this last option is the currently recommended way to create Disney/MArvel/Star Wars Villainous custom villains. Please check and ask in their discord if you are new and everyone will be happy to guide you https://discord.com/channels/701443793718870036/701449291465752596)
2. You need to install this program: https://imagemagick.org/script/download.php (choose the one for your operating system)
<br>  That program is used to generate the final images, both Fate and Villain decks, which it's like using TTS Deck builder but fully automatic, so I highly recommend having it installed.
<br>  I honestly have not tested my tool in the case this program is not installed, and never will. But everything else it does is kind of useless if you don't perform the Image Magick step so if for whatever reason you don't want to install that program, which I recommend you do and I 100% trust that program with my life, then just avoid using my tool and do it the regular slow way. I'm not your boss nor a cop, you are free to not use my tool.
3. Click here to download the compiled .jar file: https://drive.google.com/file/d/1uQjgE2b3LRI9iX0lKdxJWyK149GwilNP/view?usp=sharing
4. Place it in the folder where you are managing one of your villains. I recommend to copy this .jar file into a new folder dedicated to my tool for each of your villains. Organize it the way you desire, tho.
5. Open it so it generates the config.txt file. Please don't move that file or rename it or it will be generated again if my tool can't find it.
<br>  5.1 You will see that the first line of the config file is ignored.
<br>  5.2 On the second line, you will have to put the route to your -Exports file where you generate the images of your villain. THIS FOLDER MUST CONTAIN ONLY THE IMAGES OF THE VILLAIN YOU ARE GOING TO USE MY TOOL FOR. This is very important, because my tool is going to use these files as base for the cards that need to be used.
<br>  5.3 On the third line is going to be the name of the folder. Leave "Results" normally, or personalize it to reflect your evil impulses like "My minions".
<br>  5.4 On the fouth line, very important, there needs to be the path to a .ods file (very important that it's an .ods file, otherwise it won't know how to read it) that follows the same columns as the default "Villainous Template.ods". It MUST include at least the cards for your villain, but it doesn't really matter if it contains other villains. You can make the process slightly faster if you remove all other villains but it's hardly worth it.
<br>  5.5 On the fifth line, you may write "Autoclose" (uppercase/downcase are ignored, you may write "aUtOclOsE" if you want) so it closes automatically if everything was OK. I recommend doing it in 100% of situations, unless you don't trust me. Please somebody trust me.
6. You are done! Test it with one of your Villains that are already finished and doing their evil schemes.

## Usage

You may not use this program to generate a 3rd deck that is neither the Villain nor Fate deck. But you still may use my tool for that Villain's Fate and Villain deck normally, to do so just make sure that the value in the K column of the .ods for the cards that are neither in the Fate nor Villain decks is not a valid value to be interpreted as either Fate nor Villain (0, 1, "Villain" or "Fate", which the quoting marks of course).

If you followed the instruction, just double click in the .jar file you downloaded and it will open a window telling you what it's currently doing. Not that you need to know, but it's great to have I guess. If everything was done perfectly, it will show you a message telling you that everything was OK. If everything was OK but you are using a weird number of Fate or Villain cards, it will tell you as well (it will also prevent the window from autoclose). If everything was OK, the number of Fate and Villain cards was the expected (15 and 30 respectively). Otherwise, if there was an error, please follow the instructions that you are shown, or if you do't seem to understand them or they just make no sense please contact me on discord and I'll be so happy to help you.
