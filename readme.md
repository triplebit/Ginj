## Ginj

#### What is Ginj?

Ginj is an open source multi-platform screen capture and recording tool.

Its unobtrusive UI and no-nonsense "capture / annotate / share" workflow is hard to beat.

Ginj might remind you of a free tool called [Jing](http://web.archive.org/web/20181002042822/https://www.techsmith.com/jing-tool.html), that [Techsmith discontinued](https://feedback.techsmith.com/techsmith/topics/techsmith-jing-2019-announcement) in July 2020. But although there are similarities in the way Ging behaves, Ginj is not Jing.

###### The main differences are:
- It's all open-source. Of course, not a single line of code, not a single icon or font was taken from Jing. So basically _everything_ is different. 
- Since Screencast, Techsmith's online sharing platform, does not have a public API, I had to switch to other online sharing services. Dropbox and Google Photos are supported for now.
- Ginj will not be discontinued. It's open source. Take it, use it, keep it, fork it, improve it, change it.
- I will also probably add a few features, but I really want to stick to the no-nonsense UI I love. 
- Oh, and Ginj is written in Java.

#### Why Ginj?

- Because I was a very satisfied user of Jing and I was sad to see it go. Call me old-fashioned... 
- Because no other tool on the market satisfied me.
- Because I was looking for a challenge during the Covid-19 lockdown. That probably wasn't a good idea :-).

#### Yeah, but why the name Ginj?
- Because as I said, **G**inj **I**s **N**ot **J**ing.

#### What is the status of Ginj? 

Ginj is still under heavy work. The goal is to cover the features of the original Jing, but many features are still missing. 

However, basic still image capture, annotation and sharing works quite well already.


#### Is it multi-platform?

Ginj's core is pure Java, and Java is "write once, run anywhere", so yes, it runs on any platform... 

Just kidding.

Ginj is based on OpenJDK 14, so obviously Ginj won't ever run on a platform that does not support that JDK version. Moreover, I don't own a Mac, and I don't intend to test Ginj on a Linux VM until my to-do list becomes manageable.

In other words, until now, Ginj has only been tested on Windows.

But if you test it on other platforms, feel free to [open an issue](https://github.com/Ginj-capture/Ginj/issues) and tell me your experience. I will update this section accordingly. 

#### How can I get support?

You can't.

If you think you have found a bug, feel free to [open an issue](https://github.com/Ginj-capture/Ginj/issues), but honestly I don't intend this project to ever be bug-free or feature-complete because I know I won't have time to support it in a professional way.

And if you miss a feature, there's even less chance I will have time to look into it. But tell me anyway. 

In the meantime, it's just there and it's free... Enjoy :-)

#### Credits. 

The source code contains snippets from: 
- The Java Tutorial https://docs.oracle.com/javase/tutorial/
- Discussions at https://stackoverflow.com/
- Discussions at https://coderanch.com/

The user interface uses:
- Parts of the open source EaSynth_Look_And_Feel_Bundle.zip from 
http://www.easynth.com/freewares/EaSynthLookAndFeel.html
- Many icons from https://feathericons.com/
- Undo icon by Denis Klyuchnikov from https://thenounproject.com/
- Laptop icon from https://icons8.com/

Thanks to [Techsmith](https://www.techsmith.com/) for having proposed Jing for so long. Check out their product line.