const expression = "age >= 60 ? 'senior' : 'adult'";
Result: "senior" or "adult" depending on the age.


const expression = "membership === 'premium' ? 0.2 : 0.05";
Result: 0.2 (20% discount) or 0.05 (5% discount)

const expression = "purchaseAmount > 1000 ? 'Eligible for bonus gift' : 'Standard order'";

const expression = "age >= 18 ? { status: 'adult', access: 'full' } : { status: 'minor', access: 'restricted' }";

{ status: 'adult', access: 'full' }



const result = await Jexl.eval(rule.expression, context);
console.log(`Rule "${rule.name}" returned:`, result);
