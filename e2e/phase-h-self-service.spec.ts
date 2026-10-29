import { expect,test } from '@playwright/test';

test.beforeEach(async({page})=>{await page.goto('/login');await page.getByRole('button',{name:'Explore the fictional customer demo'}).click();await expect(page).toHaveURL(/dashboard/);});

test('mocked profile update and customer-service workflow are usable',async({page})=>{
 await page.getByRole('link',{name:/Aarav/}).click();
 await expect(page.getByRole('heading',{name:'Your banking profile'})).toBeVisible();
 await page.getByLabel('Name shown in Novara').fill('Aarav K');
 await page.getByRole('button',{name:'Save preference'}).click();
 await expect(page.getByRole('status')).toContainText('saved');
 await page.getByRole('link',{name:'Account services'}).click();
 await page.getByLabel('Description').fill('Please explain my available account information.');
 await page.getByRole('button',{name:'Submit request'}).click();
 await expect(page.getByRole('status')).toContainText('submitted');
 await expect(page.getByText('Please explain my available account information.')).toBeVisible();
});

test('mocked notifications, preferences and security center are explicit',async({page})=>{
 await page.getByRole('link',{name:'Account services'}).click();
 await page.getByLabel('Description').fill('Please provide statement assistance for my account.');
 await page.getByRole('button',{name:'Submit request'}).click();
 await page.getByRole('link',{name:'Notifications'}).first().click();
 await expect(page.getByText('Service request submitted')).toBeVisible();
 await page.getByRole('button',{name:'Mark read'}).click();
 await page.getByRole('link',{name:'Preferences'}).first().click();
 await expect(page.getByText('Mandatory security alerts remain enabled.')).toBeVisible();
 await page.getByRole('link',{name:'Security center'}).click();
 await expect(page.getByText('SANDBOX DEMO NOT PROVIDER VERIFIED')).toBeVisible();
});

test('self-service navigation reflows on mobile',async({page})=>{
 await page.setViewportSize({width:390,height:844});
 await page.getByRole('button',{name:'More'}).click();
 await page.getByRole('link',{name:'Account services'}).click();
 await expect(page.getByRole('heading',{name:'Help with your relationship'})).toBeVisible();
 const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>document.documentElement.clientWidth);
 expect(overflow).toBeFalsy();
});
