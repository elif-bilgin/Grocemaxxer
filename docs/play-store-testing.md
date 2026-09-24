# Closed testing and production access

Personal Play Console accounts created after 13 November 2023 cannot publish
to production until they have run a closed test with **at least 12 testers
opted in continuously for 14 days**, and then passed a production-access
review that takes up to about 7 days.

That is the schedule of this release. Everything else — the build, the
listing, the assets — is a day's work. The 14-day clock is three weeks of
calendar time, and it does not start until a closed-testing release is live,
which itself is blocked on finishing app setup. So the order below is not
advice, it is the dependency chain.

## The critical path

| # | Step | Blocked by | Time |
| --- | --- | --- | --- |
| 1 | Create the upload keystore | — | 5 min |
| 2 | Build a signed AAB and smoke-test it on a device | 1 | 1 hour |
| 3 | Take phone screenshots | a working build | 30 min |
| 4 | Host the privacy policy, fill in the contact email | — | 15 min |
| 5 | Finish app setup in Console (listing, data safety, content rating, target audience) | 3, 4 | 1 hour |
| 6 | Publish a closed-testing release | 2, 5 | 30 min |
| 7 | Recruit 12 testers and get them opted in | 6 | **the bottleneck** |
| 8 | Wait 14 days with all 12 continuously opted in | 7 | 14 days |
| 9 | Apply for production access | 8 | 30 min |
| 10 | Review | 9 | up to 7 days |

Steps 1–4 are in `docs/play-store-release.md`. Steps 7–9 are below.

Two things about step 8 that cost people a fortnight:

- The 14 days are **continuous**. If a tester leaves the programme on day 10
  the count does not pause, it breaks. Tell testers this explicitly.
- The count is of testers **opted in**, not testers invited. An invitation
  that nobody accepts is worth nothing. Check the opted-in number in Console
  rather than assuming.

Recruit more than 12 — 15 to 18 — so that one person dropping out or changing
their Google account does not reset three weeks.

## Recruiting testers

Testers join with the Google account they use on their phone, and that
address is what goes into the Console tester list. Collect addresses first,
add them all at once, then send the opt-in link.

A closed test accepts testers by email list or by Google Group. A Google
Group is easier to manage past about a dozen people, because adding someone
later does not need a Console change.

## Tester invitation

Something like this, adapted. The last paragraph is the one that matters.

> I've built a grocery list app called GroceMaxxer and I need 12 people to
> test it on Google Play before I'm allowed to publish it. It's free, there
> are no ads, no account, and it doesn't collect anything — it works entirely
> offline on your phone.
>
> What it does: you type in what you need to buy, and it groups the items by
> which part of the store they're in and orders those groups into one walk
> through the shop, so you don't have to double back. You can pick your
> store's layout, and you can text your list to someone else.
>
> To join, open this link on your Android phone, tap to become a tester, then
> install from Google Play: <opt-in link>
>
> Anything you notice is useful — things that look wrong, things that confuse
> you, anything that crashes. Reply here, or use "Send feedback" inside the
> Play Store listing.
>
> **Please stay in the testing programme for at least 14 days.** Google counts
> testers continuously, so if you leave early the clock restarts for everyone.
> After that you're welcome to keep the app or remove it.

## What to ask testers to exercise

The production-access application asks whether testers used all the app's
features, so ask for these specifically and keep a note of what came back.

- Add items by typing, including several at once separated by commas.
- Add items from the suggestions that appear while typing.
- Add items by browsing an aisle.
- Override an item's category when adding it.
- Check items off while actually shopping — this is the feature the app
  exists for, and it is the one that needs real-store feedback.
- Switch the store selector and see whether the order matches that shop.
- Finish a list completely and see the celebration.
- Share a list to someone else, and import one that was sent to them.
- Reopen the app the next day and use the old-list prompt.
- Change the accent colour and dark mode.

Worth asking directly, because it is the app's whole premise and the only
thing a tester can tell you that you cannot test yourself: **did the section
order actually match the shop you were in?**

## Collecting feedback

Console shows tester feedback under Monitor and improve → Ratings and reviews
→ Testing feedback. Testers can also reply to you directly.

Keep a running list of what came in and what you did about it. Both matter:
the application asks you to summarise the feedback, and separately to describe
what you changed because of it. "No feedback received" is a weak application.

## The production-access application

Three sections. Two of them can only be written after the test has actually
run; one can be written now.

### Part 1 — About your closed test

Needs real answers from the test. Record as you go:

- How easy was recruiting testers?
- Did testers use all the app's features? (The list above is your evidence.)
- Did their usage match what you expect from real users? Where did it differ?
- What feedback came in, and how did you collect it?

### Part 2 — About your app

This can be written now. Answers are not public and do not affect listing
visibility.

**Target audience.** Adults who do their own grocery shopping, particularly
people shopping large supermarkets where the walk between departments is long
enough to matter. Not directed at children. No account, so no age gate.

**Value proposition.** A shopping list normally comes out in the order you
thought of things, which is not the order the shop is laid out in, so you
criss-cross the store and double back. GroceMaxxer groups the list by
department and orders the departments into one sensible walk, with the order
adjusted per supermarket layout. It works entirely on the device, with no
account, no network access and no data collection, which also means it works
in shops with no signal.

**Estimated installs in the first year.** A free, unmonetised app with no
marketing budget: the lowest band on offer is the honest answer.

### Part 3 — About your production readiness

Needs the test to have happened:

- What did you change as a result of the closed test?
- How did you decide the app was ready for production?

If the honest answer to the first question is "nothing", that is a signal the
test was not exercised hard enough — go back to the feature list above and ask
testers for specifics, rather than applying with nothing to report.

## Before applying, re-check policy compliance

Rejections are slow to appeal, so check these rather than relying on review to
catch them:

- **Functional reliability.** No crashes, no broken screens. The pre-launch
  report in Console runs the app on real devices and is worth reading before
  you apply.
- **Test credentials.** Not applicable — the app has no login, so answer that
  all functionality is available without special access.
- **Content rating and target audience** must match what the app actually is.
- **Third-party names in the listing.** See the note in
  `docs/store-listing.md` about naming supermarket chains.
